#!/usr/bin/env python3
"""
result-summarizer.py - k6 JSON 결과 분석기

기능:
  1. k6 JSON 결과 파일에서 핵심 메트릭 추출
     - http_req_duration: avg, med, p90, p95, p99
     - http_reqs: total, rate(RPS)
     - http_req_failed: rate
     - product_response_time (커스텀 Trend)
     - product_error_rate (커스텀 Rate)
  2. Threshold PASS/FAIL 판정
  3. --compare 플래그: before vs after 비교 (개선율 자동 계산)

사용법:
  # 단일 디렉토리 분석
  python result-summarizer.py load-test/product-cache/results/

  # before vs after 비교
  python result-summarizer.py load-test/product-cache/results/ --compare

  # 특정 파일 지정
  python result-summarizer.py before_normal.json after_normal.json --compare
"""

import sys
import os
import json
import re
import glob
import argparse
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


# ── SLA 임계값 정의 ──────────────────────────────────────────────────────────
SLA_THRESHOLDS = {
    "smoke": {
        "p95":        2000,
        "error_rate": 0.01,
    },
    "normal": {
        "p95":        1000,
        "p99":        2000,
        "error_rate": 0.01,
    },
    "heavy": {
        "p95":        2000,
        "p99":        4000,
        "error_rate": 0.05,
    },
    "breakpoint": {
        "p95":        5000,
        "error_rate": 0.10,
    },
}


# ── 메트릭 추출 ───────────────────────────────────────────────────────────────

def extract_metrics(data: dict) -> dict:
    """k6 JSON summary에서 핵심 메트릭을 추출."""
    metrics = data.get("metrics", {})

    def get_trend(name: str) -> dict:
        m = metrics.get(name, {})
        v = m.get("values", {})
        return {
            "avg":  v.get("avg",  0),
            "med":  v.get("med",  0),
            "p90":  v.get("p(90)", 0),
            "p95":  v.get("p(95)", 0),
            "p99":  v.get("p(99)", 0),
            "min":  v.get("min",  0),
            "max":  v.get("max",  0),
        }

    def get_rate(name: str) -> dict:
        m = metrics.get(name, {})
        v = m.get("values", {})
        return {
            "rate":  v.get("rate", 0),
            "count": v.get("count", 0),
            "passes": v.get("passes", 0),
            "fails":  v.get("fails", 0),
        }

    def get_counter(name: str) -> dict:
        m = metrics.get(name, {})
        v = m.get("values", {})
        return {
            "count": v.get("count", 0),
            "rate":  v.get("rate", 0),
        }

    result = {
        "http_req_duration":     get_trend("http_req_duration"),
        "http_req_waiting":      get_trend("http_req_waiting"),
        "http_reqs":             get_counter("http_reqs"),
        "http_req_failed":       get_rate("http_req_failed"),
        "product_response_time": get_trend("product_response_time"),
        "product_error_rate":    get_rate("product_error_rate"),
        "vus_max": metrics.get("vus_max", {}).get("values", {}).get("max", 0),
        "iterations": metrics.get("iterations", {}).get("values", {}).get("count", 0),
    }

    # Threshold 결과 추출
    result["thresholds"] = {}
    for name, th_data in data.get("thresholds", {}).items():
        result["thresholds"][name] = th_data.get("ok", False)

    return result


def detect_test_type(filename: str) -> str:
    """파일명에서 테스트 유형 감지."""
    basename = os.path.basename(filename).lower()
    if "breakpoint" in basename:
        return "breakpoint"
    if "heavy" in basename:
        return "heavy"
    if "normal" in basename:
        return "normal"
    if "smoke" in basename:
        return "smoke"
    return "unknown"


def detect_phase(filename: str) -> str:
    """파일명에서 phase 감지."""
    basename = os.path.basename(filename).lower()
    if basename.startswith("after"):
        return "after"
    if basename.startswith("before"):
        return "before"
    return "unknown"


# ── 출력 포맷 ─────────────────────────────────────────────────────────────────

def ms_str(val: float) -> str:
    """밀리초를 가독성 있는 문자열로 변환."""
    if val >= 1000:
        return f"{val / 1000:.2f}s"
    return f"{val:.1f}ms"


def pass_fail(ok: bool) -> str:
    if ok:
        return f"{GREEN}PASS{RESET}"
    return f"{RED}FAIL{RESET}"


def improvement_str(before: float, after: float) -> str:
    """개선율 계산 및 포맷."""
    if before <= 0:
        return "N/A"
    rate = (before - after) / before * 100
    if rate > 0:
        return f"{GREEN}▼ {rate:.1f}% 개선{RESET}"
    elif rate < 0:
        return f"{RED}▲ {abs(rate):.1f}% 악화{RESET}"
    else:
        return f"{YELLOW}변화 없음{RESET}"


def print_separator(char="─", width=70):
    print(char * width)


def print_metrics_table(metrics: dict, test_type: str, phase: str, filename: str):
    """단일 파일 메트릭을 테이블로 출력."""
    dur = metrics["http_req_duration"]
    reqs = metrics["http_reqs"]
    failed = metrics["http_req_failed"]
    prod_time = metrics["product_response_time"]

    sla = SLA_THRESHOLDS.get(test_type, {})
    p95_limit = sla.get("p95", None)
    p99_limit = sla.get("p99", None)
    err_limit = sla.get("error_rate", None)

    print_separator("═")
    print(f"{BOLD}{CYAN}결과: [{phase.upper()}] {test_type.upper()}{RESET}")
    print(f"파일: {os.path.basename(filename)}")
    print_separator()

    print(f"\n{BOLD}【 응답시간 (http_req_duration) 】{RESET}")
    print(f"  avg  : {ms_str(dur['avg'])}")
    print(f"  med  : {ms_str(dur['med'])}")
    print(f"  p90  : {ms_str(dur['p90'])}")

    p95_ok = (dur['p95'] <= p95_limit) if p95_limit else True
    p99_ok = (dur['p99'] <= p99_limit) if p99_limit else True
    p95_sla = f"  (SLA: < {ms_str(p95_limit)})" if p95_limit else ""
    p99_sla = f"  (SLA: < {ms_str(p99_limit)})" if p99_limit else ""
    print(f"  p95  : {ms_str(dur['p95'])}{p95_sla}  {pass_fail(p95_ok)}")
    print(f"  p99  : {ms_str(dur['p99'])}{p99_sla}  {pass_fail(p99_ok)}")
    print(f"  max  : {ms_str(dur['max'])}")

    print(f"\n{BOLD}【 처리량 (http_reqs) 】{RESET}")
    print(f"  total: {reqs['count']:,.0f} 요청")
    print(f"  rate : {reqs['rate']:.2f} RPS")

    err_ok = (failed['rate'] <= err_limit) if err_limit else True
    err_sla = f"  (SLA: < {err_limit * 100:.0f}%)" if err_limit else ""
    print(f"\n{BOLD}【 에러율 (http_req_failed) 】{RESET}")
    print(f"  rate : {failed['rate'] * 100:.3f}%{err_sla}  {pass_fail(err_ok)}")
    print(f"  count: {failed['fails']:,.0f}건")

    if prod_time.get("avg", 0) > 0:
        print(f"\n{BOLD}【 커스텀 메트릭 (product_response_time) 】{RESET}")
        print(f"  avg  : {ms_str(prod_time['avg'])}")
        print(f"  p95  : {ms_str(prod_time['p95'])}")
        print(f"  p99  : {ms_str(prod_time['p99'])}")

    print(f"\n{BOLD}【 부하 프로파일 】{RESET}")
    print(f"  VUs 최대  : {metrics['vus_max']:.0f}")
    print(f"  반복 횟수 : {metrics['iterations']:,.0f}")

    # Threshold 요약
    if metrics.get("thresholds"):
        print(f"\n{BOLD}【 Threshold 판정 】{RESET}")
        all_pass = True
        for name, ok in metrics["thresholds"].items():
            status = pass_fail(ok)
            print(f"  {name:<45} {status}")
            if not ok:
                all_pass = False
        print(f"\n  종합 판정: {pass_fail(all_pass)}")

    print_separator("═")


def print_comparison(before_metrics: dict, after_metrics: dict,
                     test_type: str, before_file: str, after_file: str):
    """Before vs After 비교 테이블 출력."""
    bd = before_metrics["http_req_duration"]
    ad = after_metrics["http_req_duration"]
    br = before_metrics["http_reqs"]
    ar = after_metrics["http_reqs"]
    bf = before_metrics["http_req_failed"]
    af_ = after_metrics["http_req_failed"]

    print_separator("═", 80)
    print(f"{BOLD}{CYAN}Before vs After 비교: {test_type.upper()}{RESET}")
    print(f"  Before: {os.path.basename(before_file)}")
    print(f"  After : {os.path.basename(after_file)}")
    print_separator("─", 80)

    # 헤더
    print(f"\n{'메트릭':<30} {'Before':>12} {'After':>12} {'개선율':>25}")
    print("─" * 80)

    rows = [
        ("p50 (med)",  bd['med'],  ad['med']),
        ("p90",        bd['p90'],  ad['p90']),
        ("p95",        bd['p95'],  ad['p95']),
        ("p99",        bd['p99'],  ad['p99']),
        ("avg",        bd['avg'],  ad['avg']),
        ("max",        bd['max'],  ad['max']),
    ]
    for label, bval, aval in rows:
        imp = improvement_str(bval, aval)
        print(f"  {label:<28} {ms_str(bval):>12} {ms_str(aval):>12}  {imp}")

    print("─" * 80)
    # RPS 비교 (높을수록 좋음)
    rps_b, rps_a = br['rate'], ar['rate']
    rps_change = (rps_a - rps_b) / rps_b * 100 if rps_b > 0 else 0
    rps_str = (f"{GREEN}▲ {rps_change:.1f}% 향상{RESET}" if rps_change > 0
               else f"{RED}▼ {abs(rps_change):.1f}% 저하{RESET}")
    print(f"  {'RPS (처리량)':<28} {rps_b:>11.1f} {rps_a:>11.1f}  {rps_str}")

    # 에러율 비교 (낮을수록 좋음)
    err_b = bf['rate'] * 100
    err_a = af_['rate'] * 100
    err_imp = improvement_str(err_b, err_a)
    print(f"  {'에러율':<28} {err_b:>11.3f}% {err_a:>11.3f}%  {err_imp}")

    print_separator("═", 80)

    # 포트폴리오 서술 템플릿
    p95_before = bd['p95']
    p95_after  = ad['p95']
    p95_imp    = (p95_before - p95_after) / p95_before * 100 if p95_before > 0 else 0
    rps_mult   = rps_a / rps_b if rps_b > 0 else 0

    print(f"\n{BOLD}【 포트폴리오 서술 수치 】{RESET}")
    print(f"  상품 목록 API p95 응답시간: {ms_str(p95_before)} → {ms_str(p95_after)} ({p95_imp:.1f}% 개선)")
    print(f"  처리량 (RPS): {rps_b:.1f} → {rps_a:.1f} ({rps_mult:.1f}배 향상)")
    print(f"  에러율: {err_b:.3f}% → {err_a:.3f}%")
    print_separator("═", 80)


# ── 파일 탐색 ─────────────────────────────────────────────────────────────────

def find_result_files(results_dir: str):
    """results 디렉토리에서 JSON 파일을 phase/type별로 분류."""
    pattern = os.path.join(results_dir, "*.json")
    files = glob.glob(pattern)

    before_files = {}
    after_files  = {}

    for f in sorted(files):
        phase = detect_phase(f)
        ttype = detect_test_type(f)
        if phase == "before":
            before_files.setdefault(ttype, []).append(f)
        elif phase == "after":
            after_files.setdefault(ttype, []).append(f)

    return before_files, after_files


def load_json_file(filepath: str) -> Optional[dict]:
    """JSON 파일 로드. 오류 시 None 반환."""
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            return json.load(f)
    except (json.JSONDecodeError, IOError) as e:
        print(f"{RED}JSON 파싱 오류: {filepath} - {e}{RESET}")
        return None


# ── 메인 ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="k6 결과 파일 분석 및 Before/After 비교",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument(
        "results_dir_or_files",
        nargs="+",
        help="결과 디렉토리 경로 또는 JSON 파일 경로(들)",
    )
    parser.add_argument(
        "--compare",
        action="store_true",
        default=False,
        help="before vs after 비교 모드 활성화",
    )
    args = parser.parse_args()

    inputs = args.results_dir_or_files
    compare = args.compare

    print(f"\n{BOLD}{CYAN}{'=' * 70}{RESET}")
    print(f"{BOLD}{CYAN}  fit-market 부하 테스트 결과 분석기{RESET}")
    print(f"{BOLD}{CYAN}{'=' * 70}{RESET}\n")

    # ── 단일 디렉토리 모드 ──────────────────────────────────────────────────
    if len(inputs) == 1 and os.path.isdir(inputs[0]):
        results_dir = inputs[0]
        before_map, after_map = find_result_files(results_dir)

        if not before_map and not after_map:
            print(f"{YELLOW}결과 파일이 없습니다: {results_dir}{RESET}")
            sys.exit(0)

        all_types = sorted(set(list(before_map.keys()) + list(after_map.keys())))

        for ttype in all_types:
            # Before 파일 출력
            for f in before_map.get(ttype, []):
                data = load_json_file(f)
                if data:
                    metrics = extract_metrics(data)
                    print_metrics_table(metrics, ttype, "before", f)

            # After 파일 출력
            for f in after_map.get(ttype, []):
                data = load_json_file(f)
                if data:
                    metrics = extract_metrics(data)
                    print_metrics_table(metrics, ttype, "after", f)

            # 비교 (--compare 또는 both가 존재하면 자동 비교)
            if compare and before_map.get(ttype) and after_map.get(ttype):
                before_f = before_map[ttype][-1]  # 최신 파일
                after_f  = after_map[ttype][-1]

                before_data = load_json_file(before_f)
                after_data  = load_json_file(after_f)

                if before_data and after_data:
                    before_metrics = extract_metrics(before_data)
                    after_metrics  = extract_metrics(after_data)
                    print_comparison(before_metrics, after_metrics, ttype, before_f, after_f)

        # 자동 비교 (both 존재 시)
        if not compare:
            has_both = any(
                ttype in before_map and ttype in after_map
                for ttype in all_types
            )
            if has_both:
                print(f"\n{YELLOW}TIP: before와 after 파일이 모두 있습니다. --compare 옵션으로 비교할 수 있습니다.{RESET}")

    # ── 2개 파일 직접 비교 모드 ──────────────────────────────────────────────
    elif len(inputs) == 2 and all(os.path.isfile(f) for f in inputs):
        f1, f2 = inputs
        phase1 = detect_phase(f1)
        phase2 = detect_phase(f2)

        # before/after 순서 판단
        if phase1 == "after" and phase2 == "before":
            f1, f2 = f2, f1

        ttype = detect_test_type(f1)

        data1 = load_json_file(f1)
        data2 = load_json_file(f2)

        if data1 and data2:
            m1 = extract_metrics(data1)
            m2 = extract_metrics(data2)
            print_metrics_table(m1, ttype, "before", f1)
            print_metrics_table(m2, ttype, "after", f2)
            print_comparison(m1, m2, ttype, f1, f2)

    # ── 개별 파일 분석 모드 ──────────────────────────────────────────────────
    else:
        for filepath in inputs:
            if not os.path.isfile(filepath):
                print(f"{RED}파일 없음: {filepath}{RESET}")
                continue
            data = load_json_file(filepath)
            if data:
                ttype = detect_test_type(filepath)
                phase = detect_phase(filepath)
                metrics = extract_metrics(data)
                print_metrics_table(metrics, ttype, phase, filepath)


if __name__ == "__main__":
    main()
