# product-cache — 상품 캐시 성능 비교 부하 테스트

## 목적

Redis 캐싱 도입 전후의 상품 목록 API 성능을 k6로 측정하여 포트폴리오 수치를 확보한다.

## 테스트 대상 API

| API | 엔드포인트 | 쿼리 |
|-----|-----------|------|
| 상품 목록 | `GET /api/products` | 3-table JOIN × 2회 |
| 베스트 상품 | `GET /api/products/best` | 3-table JOIN × 2회 |
| 신상품 | `GET /api/products/new` | 3-table JOIN × 2회 |

## 실행 방법

### 사전 준비

```bash
# 1. Docker 실행 확인
docker --version

# 2. k6 설치 확인
k6 version

# 3. Python 설치 확인
python --version
```

### Phase 1: Before Redis (현재 상태 Baseline)

```bash
# application-loadtest.yaml에서 spring.cache.type=none 확인 후

# Smoke 테스트 (동작 확인)
./run.sh before smoke

# Normal Load (30 VU, 11분)
./run.sh before normal

# Heavy Load (200 VU, 20분)
./run.sh before heavy

# Breakpoint (RPS 한계 탐색)
./run.sh before breakpoint

# 전체 자동 실행
./run.sh before all
```

### Phase 2: After Redis (캐시 활성화)

```bash
# 1. application-loadtest.yaml에서 spring.cache.type=redis로 변경
# 2. Docker Redis 실행
docker-compose -f ../docker-compose.yml --profile redis up -d

# 3. 동일 시퀀스 실행
./run.sh after all
```

## 결과 확인

```bash
# 결과 요약
python ../scripts/result-summarizer.py results/

# Before/After 비교
python ../scripts/result-summarizer.py results/ --compare

# 캐시 히트율 (Phase 2)
redis-cli -p 6380 INFO stats | grep keyspace
```

## 파일 구조

```
product-cache/
├── README.md          # 이 파일
├── run.sh             # 7단계 오케스트레이션
├── seed-generator.py  # 시드 데이터 생성 (1000개 상품)
├── k6/
│   ├── common.js      # 공통 설정
│   ├── smoke.js       # Smoke (3 VU)
│   ├── load-normal.js # Normal (30 VU, 11분)
│   ├── load-heavy.js  # Heavy (100~200 VU, 20분)
│   └── breakpoint.js  # Breakpoint (RPS 상승)
├── data/              # 생성된 CSV + SQL (gitignore)
└── results/           # 테스트 결과 (gitignore)
```
