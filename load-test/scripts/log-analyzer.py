#!/usr/bin/env python3
"""
log-analyzer.py - Spring Boot 로그 분석기

기능:
  1. ERROR / WARN 로그 추출 및 집계
  2. HikariCP 이벤트 파싱
     - Connection timeout
     - Pool exhausted
     - Connection acquisition time
  3. 엔드포인트별 호출 통계 (REQUEST/RESPONSE 패턴 파싱)
  4. Slow query 추정
     - 동일 스레드에서 1초 이상 경과한 로그 쌍 감지

사용법:
  python log-analyzer.py load-test/product-cache/results/spring-boot.log
  python log-analyzer.py spring-boot.log --slow-threshold 2000
  python log-analyzer.py spring-boot.log --output report.txt
"""

import sys
import os
import re
import argparse
from collections import defaultdict, Counter
from datetime import datetime, timedelta
from typing import Optional

# ── 터미널 색상 ───────────────────────────────────────────────────────────────
try:
    from colorama import init as colorama_init, Fore, Style
    colorama_init(autoreset=True)
    GREEN  = Fore.GREEN
    YELLOW = Fore.YELLOW
    RED    = Fore.RED
    BLUE   = Fore.BLUE
    CYAN   = Fore.CYAN
    BOLD   = Style.BRIGHT
    RESET  = Style.RESET_ALL
except ImportError:
    GREEN = YELLOW = RED = BLUE = CYAN = BOLD = RESET = ""


# ── 정규식 패턴 ───────────────────────────────────────────────────────────────

# Spring Boot 기본 로그 형식:
# 2026-03-16 14:30:01.123  INFO 12345 --- [http-nio-8080-exec-1] c.s.f.controller.ProductController : ...
LOG_PATTERN = re.compile(
    r'^(?P<timestamp>\d{4}-\d{2}-\d{2}[\s T]\d{2}:\d{2}:\d{2}[.,]\d{3})'
    r'\s+(?P<level>TRACE|DEBUG|INFO|WARN|ERROR)'
    r'\s+\d+'
    r'\s+---\s+'
    r'\[(?P<thread>[^\]]+)\]'
    r'\s+(?P<logger>\S+)'
    r'\s+:\s+'
    r'(?P<message>.+)$'
)

# HikariCP 연결 풀 관련 패턴
HIKARI_TIMEOUT_PATTERN = re.compile(
    r'(Connection is not available|HikariPool|timeout.*pool|pool.*timeout|'
    r'Unable to acquire.*connection|connection.*timed out)',
    re.IGNORECASE
)
HIKARI_POOL_EXHAUSTED = re.compile(
    r'(pool.*exhausted|exhausted.*pool|no.*available.*connection)',
    re.IGNORECASE
)
HIKARI_ACQ_TIME = re.compile(
    r'connection.*acquired.*?(\d+)ms|(\d+)ms.*connection.*acquired',
    re.IGNORECASE
)

# SQL 실행 패턴 (MyBatis/Spring JDBC)
SQL_PATTERN = re.compile(
    r'(Preparing:|Parameters:|==>|<==\s+Total:|Executing:|SELECT|INSERT|UPDATE|DELETE)',
    re.IGNORECASE
)

# HTTP 요청/응답 패턴 (Spring MVC 디버그 로그)
HTTP_REQUEST_PATTERN = re.compile(
    r'(GET|POST|PUT|DELETE|PATCH)\s+(/api/[\w/\-?&=%.]+)',
    re.IGNORECASE
)
HTTP_RESPONSE_PATTERN = re.compile(
    r'Completed\s+(?P<status>\d{3})\s+\w+.*?(?:in\s+(?P<duration>\d+)\s*ms)?',
    re.IGNORECASE
)

# 타임스탬프 파싱
TIMESTAMP_FORMATS = [
    "%Y-%m-%d %H:%M:%S.%f",
    "%Y-%m-%d %H:%M:%S,%f",
    "%Y-%m-%dT%H:%M:%S.%f",
]


def parse_timestamp(ts_str: str) -> Optional[datetime]:
    """타임스탬프 문자열을 datetime으로 변환."""
    ts_str = ts_str.strip().replace(",", ".")
    for fmt in TIMESTAMP_FORMATS:
        try:
            return datetime.strptime(ts_str[:23], fmt[:len(ts_str[:23])])
        except ValueError:
            continue
    return None


# ── 로그 분석 클래스 ──────────────────────────────────────────────────────────

class LogAnalyzer:
    def __init__(self, log_file: str, slow_threshold_ms: int = 1000):
        self.log_file = log_file
        self.slow_threshold_ms = slow_threshold_ms

        # 집계 데이터
        self.error_lines    = []
        self.warn_lines     = []
        self.hikari_events  = []
        self.endpoint_calls = defaultdict(int)
        self.endpoint_errors = defaultdict(int)
        self.sql_counts     = Counter()
        self.slow_queries   = []

        # 스레드별 최근 타임스탬프 (slow query 감지용)
        self._thread_last_ts   = {}
        self._thread_last_line = {}

        # HTTP 요청/응답 매칭 (스레드 기반)
        self._thread_request   = {}  # thread → (endpoint, ts)

        # 전체 통계
        self.total_lines  = 0
        self.parsed_lines = 0
        self.start_time   = None
        self.end_time     = None

    def analyze(self):
        """로그 파일 전체를 파싱하여 분석."""
        if not os.path.exists(self.log_file):
            print(f"{RED}로그 파일 없음: {self.log_file}{RESET}")
            return

        file_size = os.path.getsize(self.log_file)
        print(f"로그 파일: {self.log_file} ({file_size / 1024:.1f} KB)")

        with open(self.log_file, "r", encoding="utf-8", errors="replace") as f:
            for raw_line in f:
                self.total_lines += 1
                line = raw_line.rstrip()
                self._process_line(line)

        print(f"분석 완료: {self.total_lines:,} 줄 / 파싱됨: {self.parsed_lines:,} 줄")
        if self.start_time and self.end_time:
            duration = (self.end_time - self.start_time).total_seconds()
            print(f"로그 시간 범위: {self.start_time} ~ {self.end_time} ({duration:.0f}초)")

    def _process_line(self, line: str):
        """단일 로그 줄 처리."""
        m = LOG_PATTERN.match(line)
        if not m:
            return

        self.parsed_lines += 1
        ts_str  = m.group("timestamp")
        level   = m.group("level")
        thread  = m.group("thread")
        logger  = m.group("logger")
        message = m.group("message")

        ts = parse_timestamp(ts_str)
        if ts:
            if self.start_time is None:
                self.start_time = ts
            self.end_time = ts

        # ── ERROR/WARN 수집 ───────────────────────────────────────────────
        if level == "ERROR":
            self.error_lines.append({
                "ts": ts_str, "thread": thread,
                "logger": logger, "message": message[:200],
            })
        elif level == "WARN":
            self.warn_lines.append({
                "ts": ts_str, "thread": thread,
                "logger": logger, "message": message[:200],
            })

        # ── HikariCP 이벤트 감지 ─────────────────────────────────────────
        if "hikari" in logger.lower() or "hikari" in message.lower():
            if HIKARI_TIMEOUT_PATTERN.search(message):
                self.hikari_events.append({
                    "type": "TIMEOUT",
                    "ts": ts_str, "thread": thread, "message": message[:200],
                })
            elif HIKARI_POOL_EXHAUSTED.search(message):
                self.hikari_events.append({
                    "type": "POOL_EXHAUSTED",
                    "ts": ts_str, "thread": thread, "message": message[:200],
                })
            else:
                acq_m = HIKARI_ACQ_TIME.search(message)
                if acq_m:
                    time_ms = int(acq_m.group(1) or acq_m.group(2))
                    self.hikari_events.append({
                        "type": "ACQ_TIME",
                        "ts": ts_str, "thread": thread,
                        "message": message[:200], "ms": time_ms,
                    })

        # ── HTTP 요청 감지 ────────────────────────────────────────────────
        http_m = HTTP_REQUEST_PATTERN.search(message)
        if http_m and ts:
            method   = http_m.group(1).upper()
            endpoint = http_m.group(2).split("?")[0]  # 쿼리 파라미터 제거
            full_ep  = f"{method} {endpoint}"
            self._thread_request[thread] = (full_ep, ts)
            self.endpoint_calls[full_ep] += 1

            # 에러 응답 감지 (같은 메시지에 4xx/5xx)
            if re.search(r'\b[45]\d{2}\b', message):
                self.endpoint_errors[full_ep] += 1

        # ── HTTP 응답 감지 (응답시간 포함) ────────────────────────────────
        resp_m = HTTP_RESPONSE_PATTERN.search(message)
        if resp_m and ts:
            status = int(resp_m.group("status"))
            dur_str = resp_m.group("duration")
            if status >= 400 and thread in self._thread_request:
                ep = self._thread_request[thread][0]
                self.endpoint_errors[ep] += 1

        # ── SQL 통계 ──────────────────────────────────────────────────────
        sql_m = SQL_PATTERN.search(message)
        if sql_m:
            keyword = sql_m.group(1).upper().strip().rstrip(":")
            if keyword in ("SELECT", "INSERT", "UPDATE", "DELETE"):
                self.sql_counts[keyword] += 1

        # ── Slow Query 추정 ───────────────────────────────────────────────
        # 동일 스레드에서 1초 이상 경과한 연속 로그 쌍을 감지
        if ts and thread in self._thread_last_ts:
            last_ts   = self._thread_last_ts[thread]
            last_line = self._thread_last_line[thread]
            delta_ms  = (ts - last_ts).total_seconds() * 1000

            if delta_ms >= self.slow_threshold_ms:
                self.slow_queries.append({
                    "thread":   thread,
                    "start_ts": last_ts.strftime("%H:%M:%S.%f")[:12],
                    "end_ts":   ts.strftime("%H:%M:%S.%f")[:12],
                    "delta_ms": delta_ms,
                    "prev_msg": last_line[:100],
                    "curr_msg": message[:100],
                })

        if ts:
            self._thread_last_ts[thread]   = ts
            self._thread_last_line[thread] = message

    # ── 리포트 출력 ───────────────────────────────────────────────────────────

    def print_report(self):
        """분석 결과를 콘솔에 출력."""
        sep = "─" * 70

        print(f"\n{BOLD}{CYAN}{'=' * 70}{RESET}")
        print(f"{BOLD}{CYAN}  Spring Boot 로그 분석 리포트{RESET}")
        print(f"{BOLD}{CYAN}{'=' * 70}{RESET}")

        # 1. ERROR/WARN 요약
        print(f"\n{BOLD}【 1. ERROR / WARN 집계 】{RESET}")
        print(f"  ERROR: {RED}{len(self.error_lines)}건{RESET}")
        print(f"  WARN : {YELLOW}{len(self.warn_lines)}건{RESET}")

        if self.error_lines:
            print(f"\n{RED}  ERROR 로그 (최대 10건):{RESET}")
            for e in self.error_lines[:10]:
                print(f"    [{e['ts']}] [{e['thread'][:20]}] {e['message'][:100]}")
            if len(self.error_lines) > 10:
                print(f"    ... 외 {len(self.error_lines) - 10}건")

        if self.warn_lines:
            # WARN은 중복 패턴 집계
            warn_messages = Counter(w["message"][:60] for w in self.warn_lines)
            print(f"\n{YELLOW}  WARN 패턴 상위 5개:{RESET}")
            for msg, cnt in warn_messages.most_common(5):
                print(f"    {cnt:>5}회  {msg}")

        # 2. HikariCP 이벤트
        print(f"\n{BOLD}【 2. HikariCP 이벤트 】{RESET}")
        if self.hikari_events:
            type_counts = Counter(e["type"] for e in self.hikari_events)
            for etype, cnt in type_counts.items():
                color = RED if etype in ("TIMEOUT", "POOL_EXHAUSTED") else YELLOW
                print(f"  {color}{etype}: {cnt}건{RESET}")

            # 연결 획득 시간 통계
            acq_times = [e["ms"] for e in self.hikari_events if e["type"] == "ACQ_TIME"]
            if acq_times:
                avg_acq = sum(acq_times) / len(acq_times)
                max_acq = max(acq_times)
                print(f"  연결 획득 시간: avg={avg_acq:.1f}ms, max={max_acq:.1f}ms ({len(acq_times)}건)")

            # 타임아웃 최초 발생 시각
            timeouts = [e for e in self.hikari_events if e["type"] == "TIMEOUT"]
            if timeouts:
                print(f"  {RED}첫 연결 타임아웃: {timeouts[0]['ts']}{RESET}")
        else:
            print(f"  {GREEN}HikariCP 이벤트 없음 (정상){RESET}")

        # 3. 엔드포인트별 호출 통계
        print(f"\n{BOLD}【 3. 엔드포인트별 호출 통계 】{RESET}")
        if self.endpoint_calls:
            print(f"  {'엔드포인트':<45} {'호출':>8} {'에러':>8} {'에러율':>8}")
            print(f"  {sep}")
            for ep, cnt in sorted(self.endpoint_calls.items(),
                                   key=lambda x: -x[1])[:20]:
                err_cnt  = self.endpoint_errors.get(ep, 0)
                err_rate = err_cnt / cnt * 100 if cnt > 0 else 0
                err_color = RED if err_rate > 5 else (YELLOW if err_rate > 1 else GREEN)
                print(f"  {ep:<45} {cnt:>8,} {err_cnt:>8,} {err_color}{err_rate:>7.1f}%{RESET}")
        else:
            print("  엔드포인트 호출 패턴이 감지되지 않았습니다.")
            print("  (Spring MVC 디버그 로그가 활성화되어 있어야 합니다)")

        # 4. SQL 통계
        print(f"\n{BOLD}【 4. SQL 통계 (추정) 】{RESET}")
        if self.sql_counts:
            total_sql = sum(self.sql_counts.values())
            for keyword, cnt in self.sql_counts.most_common():
                pct = cnt / total_sql * 100
                print(f"  {keyword:<10}: {cnt:>8,}회 ({pct:.1f}%)")
            print(f"  {'합계':<10}: {total_sql:>8,}회")
        else:
            print("  SQL 패턴이 감지되지 않았습니다.")
            print("  (MyBatis 로그 레벨이 DEBUG여야 합니다)")

        # 5. Slow Query 추정
        print(f"\n{BOLD}【 5. Slow Query 추정 (스레드 내 {self.slow_threshold_ms}ms+ 지연) 】{RESET}")
        if self.slow_queries:
            # 지연시간 기준 상위 정렬
            slow_sorted = sorted(self.slow_queries, key=lambda x: -x["delta_ms"])
            print(f"  총 {len(slow_sorted)}건 감지됨")
            print(f"\n  상위 10건:")
            for sq in slow_sorted[:10]:
                color = RED if sq["delta_ms"] >= 3000 else YELLOW
                print(f"  {color}[{sq['start_ts']} → {sq['end_ts']}] "
                      f"{sq['delta_ms']:.0f}ms [{sq['thread'][:25]}]{RESET}")
                print(f"    Before: {sq['prev_msg']}")
                print(f"    After : {sq['curr_msg']}")
                print()

            # 지연 분포
            buckets = Counter()
            for sq in self.slow_queries:
                d = sq["delta_ms"]
                if d < 2000:   buckets["1~2s"]    += 1
                elif d < 5000: buckets["2~5s"]    += 1
                elif d < 10000: buckets["5~10s"]  += 1
                else:          buckets["10s+"]    += 1
            print(f"  지연 분포:")
            for bucket, cnt in sorted(buckets.items()):
                print(f"    {bucket}: {cnt}건")
        else:
            print(f"  {GREEN}{self.slow_threshold_ms}ms 이상 지연 없음 (정상){RESET}")

        # 6. 종합 판정
        print(f"\n{BOLD}【 6. 종합 판정 】{RESET}")
        issues = []
        if self.error_lines:
            issues.append(f"ERROR {len(self.error_lines)}건 발생")
        hikari_critical = [e for e in self.hikari_events
                           if e["type"] in ("TIMEOUT", "POOL_EXHAUSTED")]
        if hikari_critical:
            issues.append(f"HikariCP 연결 문제 {len(hikari_critical)}건")
        if self.slow_queries:
            critical_slow = [s for s in self.slow_queries if s["delta_ms"] >= 5000]
            if critical_slow:
                issues.append(f"5초 이상 Slow Query {len(critical_slow)}건")

        if issues:
            print(f"  {RED}주의 필요: {', '.join(issues)}{RESET}")
        else:
            print(f"  {GREEN}이상 없음 (오류 없음, HikariCP 정상, Slow Query 없음){RESET}")

        print(f"\n{BOLD}{CYAN}{'=' * 70}{RESET}\n")

    def save_report(self, output_file: str):
        """리포트를 파일로 저장."""
        import io
        old_stdout = sys.stdout
        sys.stdout = buffer = io.StringIO()
        try:
            self.print_report()
        finally:
            sys.stdout = old_stdout

        content = buffer.getvalue()
        # ANSI 코드 제거
        ansi_escape = re.compile(r'\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])')
        clean_content = ansi_escape.sub("", content)

        with open(output_file, "w", encoding="utf-8") as f:
            f.write(clean_content)
        print(f"리포트 저장됨: {output_file}")


# ── 메인 ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Spring Boot 로그 분석기 (ERROR/WARN, HikariCP, 엔드포인트, Slow Query)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument(
        "log_file",
        help="분석할 Spring Boot 로그 파일 경로",
    )
    parser.add_argument(
        "--slow-threshold",
        type=int,
        default=1000,
        metavar="MS",
        help="Slow Query 감지 기준 밀리초 (기본: 1000ms)",
    )
    parser.add_argument(
        "--output",
        type=str,
        default=None,
        metavar="FILE",
        help="리포트를 파일로 저장 (지정하지 않으면 콘솔 출력)",
    )

    args = parser.parse_args()

    analyzer = LogAnalyzer(args.log_file, slow_threshold_ms=args.slow_threshold)
    analyzer.analyze()
    analyzer.print_report()

    if args.output:
        analyzer.save_report(args.output)


if __name__ == "__main__":
    main()
