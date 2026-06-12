# Rules Index

이 문서는 v3 BASE PROJECT에서 Codex, Claude, 폐쇄망 AI가 따라야 할 규칙의 위치와 우선순위를 정리한다.

## 문서 계층

| 위치 | 용도 |
|---|---|
| `AGENTS.md` | Codex repo-level 진입 규칙 |
| `CLAUDE.md` | Claude Code repo-level 진입 규칙 |
| `docs/base/*.md` | 공통 설계 원문 |
| `.claude/rules/*.md` | Claude Code path-scoped 실행 규칙 |

## 필수 읽기 순서

1. `AGENTS.md` 또는 `CLAUDE.md`
2. `docs/base/README.md`
3. `docs/base/environment-auth-policy.md`
4. `docs/base/emp-dep-identity-policy.md`
5. `docs/base/menu-authority-table-design.md`
6. `docs/base/menu-source-policy.md`
7. 작업 종류에 맞는 `.claude/rules/*.md`

메뉴 seed 또는 메뉴/권한 DDL 작업 시 `docs/base/v2-menu-baseline.md`와 `docs/base/dba-request-menu-auth-ddl.md`를 추가로 읽는다.

화면(Thymeleaf/JS/CSS) 작업 시 `docs/base/screen-convention.md`를 추가로 읽는다.

JSON endpoint(목록 조회/저장/삭제 API) 작업 시 `docs/base/common-response-contract.md`를 추가로 읽는다.

업무 화면 생성 시 `docs/base/screen-generation-guide.md`를 절차 기준으로 따르고, `docs/base/domain-rules.md`의 해당 도메인 섹션을 함께 읽는다.

개인정보가 포함된 화면 작업 시 `docs/base/audit-masking-policy.md`를 추가로 읽는다.

진입 문서 간 읽기 순서가 다르게 보이면 이 문서(`rules-index.md`)를 기준으로 한다.

## 규칙 작성 원칙

- `docs/base`는 상세 설계와 결정 근거를 담는다.
- `.claude/rules`는 낮은 모델도 바로 따를 수 있는 짧은 실행 규칙만 담는다.
- 같은 내용이 충돌하면 `docs/base`의 설계 원문을 우선하고, `.claude/rules`를 즉시 갱신한다.
- 문서와 코드가 충돌하면 작업을 멈추고 충돌 내용을 보고한다. 임의로 한쪽을 선택하지 않는다.

## 완료 기준

코드 변경이 있는 작업은 다음을 모두 통과해야 완료다.

```text
mvn test
mvn -DskipTests package
```

문서만 변경한 작업은 Maven 실행 대상이 아니다. 단, 문서가 코드 동작을 바꾸도록 지시하는 경우에는 코드 반영 전까지 부분 완료로 보고한다.