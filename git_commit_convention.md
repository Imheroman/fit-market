# 깃 커밋 규칙

## 타입별 설명

| 타입    | 설명                                   |
|---------|--------------------------------------|
| feat    | 새로운 기능 추가                      |
| fix     | 버그 수정                            |
| docs    | 문서 변경                            |
| style   | 코드 포맷 등 스타일 변경 (기능 변경 없음) |
| refactor| 코드 리팩토링 (기능 변경 없음)        |
| perf    | 성능 개선                            |
| test    | 테스트 추가/수정                     |
| chore   | 기타 변경 (빌드, 패키지 매니저 수정 등) |
| build   | 빌드 관련 변경                      |
| ci      | CI 관련 변경                        |
| revert  | 이전 커밋 되돌리기                  |

---

## 작성 규칙

- 제목은 50자 내외, 소문자 시작, 마침표 없음  
- 본문은 72자 이내 줄바꿈  
- 본문에 무엇을, 왜 변경했는지 명확하게 작성  
- 빈 줄로 구분해 가독성 유지  
- 브레이킹 체인지 및 이슈 번호는 footer에 명시

---

## 예시

```md
feat(auth): add JWT token authentication
- 로그인 성공 시 JWT 토큰 발급 기능 추가
- 기존 세션 방식 폐기 예정
```

```md
fix(parser): handle null pointer exception
- 입력값이 null일 때 발생하던 예외 처리 수정
```

```md
docs(gradle): update installation instructions
- 설치 과정에서 필요한 추가 의존성 명시
```

## 기타 팁

- 잘못된 커밋 메시지는 `git rebase -i`로 수정 가능  
- 팀 정책에 맞게 scope를 적극 활용  
- 커밋 메시지는 프로젝트 자동화(릴리즈 노트 생성 등)에 활용 가능  
