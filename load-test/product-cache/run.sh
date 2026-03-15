#!/usr/bin/env bash
# run.sh — 부하 테스트 7단계 오케스트레이션
#
# 사용법:
#   ./run.sh [before|after] [smoke|normal|heavy|breakpoint|all]
#
# 예시:
#   ./run.sh before smoke        # Smoke 테스트만 실행
#   ./run.sh before normal       # Normal Load (baseline 측정)
#   ./run.sh before all          # 전체 순서 실행
#   ./run.sh after normal        # Redis 도입 후 Normal Load 비교
#   ./run.sh after all           # Redis 도입 후 전체 비교
#
# 환경 요구사항:
#   - Docker Desktop 실행 중
#   - k6 설치됨
#   - Python 3.x 설치됨
#   - mysql CLI 설치됨 (MySQL 클라이언트)

set -euo pipefail

# ── 파라미터 ────────────────────────────────────────────────────────────────
PHASE="${1:-before}"   # before | after
TEST="${2:-smoke}"     # smoke | normal | heavy | breakpoint | all

# ── 경로 설정 ────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="${SCRIPT_DIR}/results"
K6_DIR="${SCRIPT_DIR}/k6"
DATA_DIR="${SCRIPT_DIR}/data"
LOAD_TEST_DIR="${SCRIPT_DIR}/.."
FITMARKET_BE_DIR="${SCRIPT_DIR}/../../fitmarket_be"

BASE_URL="${BASE_URL:-http://localhost:8080}"
MYSQL_HOST="127.0.0.1"
MYSQL_PORT="3307"
MYSQL_USER="loadtest"
MYSQL_PASS="loadtest"
MYSQL_DB="fitmarket_loadtest"

# ── 색상 출력 ────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'  # No Color

log()   { echo -e "${GREEN}[$(date +%H:%M:%S)] ✓ $1${NC}"; }
warn()  { echo -e "${YELLOW}[$(date +%H:%M:%S)] ⚠ $1${NC}"; }
error() { echo -e "${RED}[$(date +%H:%M:%S)] ✗ $1${NC}"; exit 1; }
info()  { echo -e "${BLUE}[$(date +%H:%M:%S)] ℹ $1${NC}"; }

# ── 배너 ────────────────────────────────────────────────────────────────────
echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║          fit-market 부하 테스트 오케스트레이터              ║"
echo "║          시나리오: product-cache                            ║"
echo "╠══════════════════════════════════════════════════════════════╣"
echo "║  Phase : ${PHASE}                                                "
echo "║  Test  : ${TEST}                                                "
echo "║  URL   : ${BASE_URL}                                        "
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# ── results 디렉토리 보장 ────────────────────────────────────────────────────
mkdir -p "${RESULTS_DIR}"

# ════════════════════════════════════════════════════════════════════════════
# Step 1: 사전 조건 확인
# ════════════════════════════════════════════════════════════════════════════
log "Step 1: Prerequisites 확인"

command -v docker &>/dev/null || error "Docker가 설치되어 있지 않습니다. Docker Desktop을 설치 후 실행해주세요."
command -v k6     &>/dev/null || error "k6가 설치되어 있지 않습니다. https://k6.io/docs/getting-started/installation/"
command -v python &>/dev/null || command -v python3 &>/dev/null || error "Python 3.x가 설치되어 있지 않습니다."
command -v mysql  &>/dev/null || error "mysql CLI가 설치되어 있지 않습니다. (MySQL 클라이언트 패키지 필요)"

# Python 명령 감지
PYTHON_CMD="python"
if ! command -v python &>/dev/null; then
    PYTHON_CMD="python3"
fi

# Phase 값 검증
if [[ "${PHASE}" != "before" && "${PHASE}" != "after" ]]; then
    error "PHASE는 'before' 또는 'after'여야 합니다. (입력값: ${PHASE})"
fi

# TEST 값 검증
if [[ "${TEST}" != "smoke" && "${TEST}" != "normal" && \
      "${TEST}" != "heavy" && "${TEST}" != "breakpoint" && "${TEST}" != "all" ]]; then
    error "TEST는 smoke|normal|heavy|breakpoint|all 중 하나여야 합니다. (입력값: ${TEST})"
fi

log "Prerequisites 확인 완료"

# ════════════════════════════════════════════════════════════════════════════
# 정리 함수 — 스크립트 종료 시(정상/비정상) Spring Boot 프로세스 자동 정리
# ════════════════════════════════════════════════════════════════════════════
SPRING_PID=""  # trap에서 참조하는 전역 변수 초기화

cleanup() {
    if [[ -n "${SPRING_PID}" ]]; then
        log "정리(trap): Spring Boot 종료 (PID: ${SPRING_PID})"
        kill "${SPRING_PID}" 2>/dev/null || true
    fi
}
trap cleanup EXIT

# ════════════════════════════════════════════════════════════════════════════
# Step 2: Docker MySQL 시작
# ════════════════════════════════════════════════════════════════════════════
log "Step 2: Docker MySQL 시작 (port ${MYSQL_PORT})"

# Phase 2 (after) 이고 Redis도 필요한 경우
if [[ "${PHASE}" == "after" ]]; then
    warn "Phase 'after': Docker Redis도 함께 시작합니다 (port 6380)"
    docker-compose -f "${LOAD_TEST_DIR}/docker-compose.yml" --profile redis up -d mysql redis \
        || error "Docker 컨테이너 시작 실패"
else
    docker-compose -f "${LOAD_TEST_DIR}/docker-compose.yml" up -d mysql \
        || error "Docker MySQL 시작 실패"
fi

# MySQL 헬스체크 대기
info "MySQL 준비 대기 중 (최대 60초)..."
MYSQL_READY=0
for i in $(seq 1 12); do
    if mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" \
             -u "${MYSQL_USER}" -p"${MYSQL_PASS}" \
             -e "SELECT 1;" "${MYSQL_DB}" &>/dev/null 2>&1; then
        MYSQL_READY=1
        break
    fi
    info "MySQL 응답 대기 중... (${i}/12, 5초 후 재시도)"
    sleep 5
done

if [[ "${MYSQL_READY}" -eq 0 ]]; then
    error "MySQL이 60초 내에 준비되지 않았습니다. Docker 상태를 확인해주세요."
fi
log "MySQL 준비 완료"

# ════════════════════════════════════════════════════════════════════════════
# Step 3: 시드 데이터 생성
# ════════════════════════════════════════════════════════════════════════════
log "Step 3: 시드 데이터 생성 (1,000개 상품)"

"${PYTHON_CMD}" "${SCRIPT_DIR}/seed-generator.py" \
    || error "시드 데이터 생성 실패 (seed-generator.py)"

log "시드 데이터 CSV + SQL 생성 완료"

# ════════════════════════════════════════════════════════════════════════════
# Step 4: Spring Boot 시작
# ════════════════════════════════════════════════════════════════════════════
log "Step 4: Spring Boot 시작 (loadtest profile)"

if [[ ! -d "${FITMARKET_BE_DIR}" ]]; then
    error "fitmarket_be 디렉토리를 찾을 수 없습니다: ${FITMARKET_BE_DIR}"
fi

# 이미 실행 중인지 확인
if curl -s --max-time 3 "${BASE_URL}/api/actuator/health" &>/dev/null; then
    warn "Spring Boot가 이미 실행 중입니다 (${BASE_URL}). 기존 프로세스를 사용합니다."
    SPRING_PID=""
else
    SPRING_LOG="${RESULTS_DIR}/spring-boot.log"
    info "Spring Boot 로그 → ${SPRING_LOG}"

    (
        cd "${FITMARKET_BE_DIR}"
        ./gradlew bootRun --args='--spring.profiles.active=loadtest' \
            > "${SPRING_LOG}" 2>&1
    ) &
    SPRING_PID=$!
    info "Spring Boot PID: ${SPRING_PID}"

    # Spring Boot 시작 대기 (최대 120초)
    info "Spring Boot 시작 대기 중 (최대 120초)..."
    SPRING_READY=0
    for i in $(seq 1 24); do
        if curl -s --max-time 5 "${BASE_URL}/api/actuator/health" &>/dev/null; then
            SPRING_READY=1
            break
        fi
        # /api/products로도 헬스체크 시도
        if curl -s --max-time 5 "${BASE_URL}/api/products?page=1&size=1" &>/dev/null; then
            SPRING_READY=1
            break
        fi
        info "Spring Boot 응답 대기 중... (${i}/24, 5초 후 재시도)"
        sleep 5
    done

    if [[ "${SPRING_READY}" -eq 0 ]]; then
        warn "Spring Boot 헬스체크 응답 없음. 로그를 확인해주세요: ${SPRING_LOG}"
        warn "10초 더 대기 후 진행합니다..."
        sleep 10
    else
        log "Spring Boot 준비 완료"
    fi
fi

# ════════════════════════════════════════════════════════════════════════════
# Step 5: 시드 데이터 적재
# ════════════════════════════════════════════════════════════════════════════
log "Step 5: 시드 데이터 적재 (MySQL)"

SEED_SQL="${DATA_DIR}/seed-data.sql"
if [[ ! -f "${SEED_SQL}" ]]; then
    error "seed-data.sql이 없습니다: ${SEED_SQL}"
fi

mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" \
      -u "${MYSQL_USER}" -p"${MYSQL_PASS}" \
      "${MYSQL_DB}" \
      --local-infile=1 \
      < "${SEED_SQL}" \
    || error "시드 데이터 적재 실패. MySQL 로그를 확인해주세요."

log "시드 데이터 적재 완료"

# 적재 결과 확인
PRODUCT_COUNT=$(mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" \
    -u "${MYSQL_USER}" -p"${MYSQL_PASS}" \
    "${MYSQL_DB}" --silent --skip-column-names \
    -e "SELECT COUNT(*) FROM products WHERE id >= 1000000;" 2>/dev/null || echo "0")
info "적재된 상품 수: ${PRODUCT_COUNT}개"

if [[ "${PRODUCT_COUNT}" -lt 10000 ]]; then
    warn "적재된 상품 수(${PRODUCT_COUNT})가 예상보다 적습니다. 데이터를 확인해주세요."
fi

# Phase 2: Redis 캐시 초기화
if [[ "${PHASE}" == "after" ]]; then
    info "Phase 'after': Redis 캐시 초기화 (FLUSHALL)"
    redis-cli -p 6380 FLUSHALL &>/dev/null \
        && log "Redis 캐시 초기화 완료" \
        || warn "Redis FLUSHALL 실패 (redis-cli가 없거나 Redis가 실행 중이지 않을 수 있음)"
fi

# ════════════════════════════════════════════════════════════════════════════
# Step 5.5: Smoke 테스트 (동작 확인)
# ════════════════════════════════════════════════════════════════════════════
log "Step 5.5: Smoke 테스트 (동작 확인)"

# 타임스탬프: Smoke(Step 5.5) + 부하 테스트(Step 6) 결과 파일명에 공통 사용
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

k6 run \
    --env BASE_URL="${BASE_URL}" \
    --env PHASE="${PHASE}" \
    --env RESULTS_DIR="${RESULTS_DIR}" \
    --out "json=${RESULTS_DIR}/${PHASE}_smoke_${TIMESTAMP}.json" \
    "${K6_DIR}/smoke.js" \
    || error "Smoke 테스트 실패. API 동작을 확인해주세요."

log "Smoke 테스트 PASS"

# ════════════════════════════════════════════════════════════════════════════
# Step 6: 부하 테스트 실행
# ════════════════════════════════════════════════════════════════════════════

# 개별 테스트 실행 함수
run_test() {
    local test_type="$1"
    local script_name="$2"
    local result_file="${RESULTS_DIR}/${PHASE}_${test_type}_${TIMESTAMP}.json"

    log "Step 6: [${PHASE}] ${test_type} 테스트 실행"
    info "결과 파일: ${result_file}"

    k6 run \
        --env BASE_URL="${BASE_URL}" \
        --env PHASE="${PHASE}" \
        --env RESULTS_DIR="${RESULTS_DIR}" \
        --out "json=${result_file}" \
        "${K6_DIR}/${script_name}" \
        || warn "${test_type} 테스트가 threshold를 초과했습니다 (결과는 저장됨)"

    log "${test_type} 테스트 완료 → ${result_file}"
}

case "${TEST}" in
    smoke)
        info "Smoke 테스트는 Step 5.5에서 이미 실행되었습니다."
        ;;
    normal)
        run_test "normal" "load-normal.js"
        ;;
    heavy)
        run_test "heavy" "load-heavy.js"
        ;;
    breakpoint)
        run_test "breakpoint" "breakpoint.js"
        ;;
    all)
        run_test "normal" "load-normal.js"

        info "서버 회복 대기 (60초)..."
        sleep 60

        run_test "heavy" "load-heavy.js"

        info "서버 회복 대기 (60초)..."
        sleep 60

        run_test "breakpoint" "breakpoint.js"
        ;;
    *)
        error "알 수 없는 테스트 유형: ${TEST}"
        ;;
esac

# ════════════════════════════════════════════════════════════════════════════
# Step 7: 결과 분석 & 정리
# ════════════════════════════════════════════════════════════════════════════
log "Step 7: 결과 분석"

SUMMARIZER="${SCRIPT_DIR}/../scripts/result-summarizer.py"
if [[ -f "${SUMMARIZER}" ]]; then
    "${PYTHON_CMD}" "${SUMMARIZER}" "${RESULTS_DIR}" --compare \
        || warn "result-summarizer.py 실행 중 오류 발생"
else
    warn "result-summarizer.py를 찾을 수 없습니다: ${SUMMARIZER}"
fi

# Phase 2: Redis 캐시 히트율 출력
if [[ "${PHASE}" == "after" ]]; then
    info "Redis 캐시 통계:"
    redis-cli -p 6380 INFO stats 2>/dev/null | grep -E "keyspace_hits|keyspace_misses" \
        || warn "Redis 통계 조회 실패"
fi

# Spring Boot 종료
if [[ -n "${SPRING_PID:-}" ]]; then
    log "정리: Spring Boot 종료 (PID: ${SPRING_PID})"
    kill "${SPRING_PID}" 2>/dev/null || true
    sleep 3
fi

# 테스트 데이터 정리 (ID 1,000,000+)
log "정리: 테스트 데이터 삭제 (ID >= 1,000,000)"
mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" \
      -u "${MYSQL_USER}" -p"${MYSQL_PASS}" \
      "${MYSQL_DB}" -e "
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM products WHERE id >= 1000000;
DELETE FROM food     WHERE id >= 1000000;
DELETE FROM sellers  WHERE id >= 1000000;
DELETE FROM users    WHERE id >= 1000000;
SET FOREIGN_KEY_CHECKS = 1;
SELECT '정리 완료' AS status;
" 2>/dev/null || warn "테스트 데이터 정리 실패 (수동으로 삭제해주세요)"

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
warn "Docker 컨테이너는 수동으로 종료하세요:"
warn "  docker-compose -f load-test/docker-compose.yml down"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
log "완료! 결과 파일 경로: ${RESULTS_DIR}"
ls -la "${RESULTS_DIR}"/*.json 2>/dev/null || info "(JSON 결과 파일 없음)"
