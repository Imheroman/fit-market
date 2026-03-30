# product-cache — 상품 캐시 성능 비교 부하 테스트

## 목적

Redis 캐싱 도입 전후의 상품 목록 API 성능을 k6로 측정하여 포트폴리오 수치를 확보한다.

## 테스트 대상 API

| API | 엔드포인트 | 쿼리 |
|-----|-----------|------|
| 상품 목록 | `GET /api/products` | 3-table JOIN × 2회 |
| 베스트 상품 | `GET /api/products/best` | 3-table JOIN × 2회 |
| 신상품 | `GET /api/products/new` | 3-table JOIN × 2회 |

## 테스트 시나리오

| 시나리오 | 파일 | VU / 부하 | 시간 | 목적 |
|---------|------|----------|------|------|
| Smoke | `k6/smoke.js` | 3 VU × 5회 반복 | ~2분 | API 동작 확인 |
| Normal Load | `k6/load-normal.js` | 10→30 VU | 11분 | 일상 트래픽 기준선 |
| Heavy Load | `k6/load-heavy.js` | 30→100→200 VU | 20분 | 이벤트/세일 시뮬레이션 |
| Breakpoint | `k6/breakpoint.js` | 10→200 RPS | ~10분 | 시스템 한계점 탐색 |

## 사전 준비

```bash
# 1. Docker 실행 확인
docker --version

# 2. k6 설치 확인
k6 version

# 3. Python 설치 확인 (결과 분석용)
python --version
```

## 실행 방법

### 1. 부하테스트 전용 DB 기동

기존 개발용 Docker 컨테이너(MySQL 3306, Redis 6379)는 **종료하지 않아도 된다.**
부하테스트 전용 인프라는 별도 포트를 사용하므로 충돌하지 않는다.

| 구분 | MySQL | Redis |
|------|-------|-------|
| 개발용 (기존) | 3306 | 6379 |
| 부하테스트용 | 3308 | 6380 |

```bash
# 프로젝트 루트에서 실행
# 부하테스트 전용 MySQL(3308) 기동
docker-compose up -d mysql-loadtest
```

### 2. Spring Boot 서버 기동

> **주의:** 개발용 Spring Boot가 8080 포트에서 실행 중이면 **먼저 종료**해야 한다.
> 부하테스트 서버도 동일하게 8080 포트를 사용한다.

```bash
# load-test 디렉토리에서 실행
bash load-test/start-server.sh
```

서버가 `loadtest` 프로파일로 기동되어 `localhost:3308/fitmarket` DB에 연결된다.

### 3. Phase 1: Before Redis (캐시 비활성화 Baseline)

`application-loadtest.yaml`에서 `spring.cache.type=none` 확인 후 실행한다.
**새 터미널**을 열어 k6 테스트를 실행한다.

```bash
cd load-test/product-cache

# Smoke 테스트 (동작 확인, ~2분)
k6 run -e PHASE=before -e RESULTS_DIR=results k6/smoke.js

# Normal Load (30 VU, 11분)
k6 run -e PHASE=before -e RESULTS_DIR=results k6/load-normal.js

# Heavy Load (200 VU, 20분)
k6 run -e PHASE=before -e RESULTS_DIR=results k6/load-heavy.js

# Breakpoint (RPS 한계 탐색, ~10분)
k6 run -e PHASE=before -e RESULTS_DIR=results k6/breakpoint.js
```

### 4. Phase 2: After Redis (캐시 활성화)

```bash
# 1. application-loadtest.yaml에서 spring.cache.type=redis로 변경

# 2. Redis 컨테이너 기동 (port 6380)
docker-compose -f load-test/docker-compose.yml --profile redis up -d

# 3. Spring Boot 서버 재시작
bash load-test/start-server.sh

# 4. 동일 시퀀스를 PHASE=after로 실행
cd load-test/product-cache
k6 run -e PHASE=after -e RESULTS_DIR=results k6/smoke.js
k6 run -e PHASE=after -e RESULTS_DIR=results k6/load-normal.js
k6 run -e PHASE=after -e RESULTS_DIR=results k6/load-heavy.js
k6 run -e PHASE=after -e RESULTS_DIR=results k6/breakpoint.js
```

### PHASE 환경 변수

`PHASE`는 테스트 동작을 변경하지 않는다. **결과 파일 이름에만 영향**을 준다.

- `PHASE=before` → `results/before_smoke_2026-03-22T...json`
- `PHASE=after` → `results/after_smoke_2026-03-22T...json`

이렇게 파일명을 구분해두면 `result-summarizer.py --compare`로 before/after 결과를 자동 비교할 수 있다.

## 결과 확인

```bash
# 결과 요약
python load-test/scripts/result-summarizer.py load-test/product-cache/results/

# Before/After 비교
python load-test/scripts/result-summarizer.py load-test/product-cache/results/ --compare

# 캐시 히트율 확인 (Phase 2)
redis-cli -p 6380 INFO stats | grep keyspace
```

## 파일 구조

```
load-test/
├── start-server.sh              # Spring Boot loadtest 프로파일 기동
├── scripts/
│   ├── result-summarizer.py     # k6 결과 분석/비교
│   └── log-analyzer.py          # Spring Boot 로그 분석
└── product-cache/
    ├── README.md                # 이 파일
    ├── k6/
    │   ├── common.js            # 공통 설정 (BASE_URL, testParams)
    │   ├── smoke.js             # Smoke (3 VU, ~2분)
    │   ├── load-normal.js       # Normal (10→30 VU, 11분)
    │   ├── load-heavy.js        # Heavy (30→200 VU, 20분)
    │   └── breakpoint.js        # Breakpoint (10→200 RPS)
    ├── data/
    │   ├── test-params.csv      # API 테스트 케이스 (14개)
    │   ├── products.csv         # 상품 시드 데이터 (1000개)
    │   ├── sellers.csv          # 판매자 계정 데이터
    │   ├── food.csv             # 음식 분류 데이터
    │   └── seed-data.sql        # DB 시드 로드 스크립트
    └── results/                 # 테스트 결과 JSON (gitignore)
```
