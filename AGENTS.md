# SMS V3 Codex 작업 규칙

이 저장소는 SMS/LMS/알림톡 대량 발송 및 이력 관리 시스템의 v3 BASE PROJECT를 만들기 위한 작업 공간이다. 목표는 화면을 많이 만드는 것이 아니라, 폐쇄망에서도 반복 생성과 검증이 가능한 BASE 구조를 먼저 완성하는 것이다.

## 기술 스택

- Spring Boot 3.3.0 / Java 21 (JDK 21 필수)
- MyBatis 3.0.3 + Oracle 19c (`ojdbc11`)
- Thymeleaf + `thymeleaf-layout-dialect`
- Spring Security + Spring Security LDAP
- Lombok, Apache POI 5.2.3(Excel), jsqlparser 4.9(Query Scaffold)
- WAR 패키징 → 외부 Tomcat 배포(`ServletInitializer` 제공). 로컬은 `spring-boot-devtools`로 기동
- 신규 코드 패키지 루트: `com.scbk.sms`

## 개발 명령

- 빌드: `mvn -DskipTests package`
- 테스트 전체: `mvn test`
- 단일 테스트: `mvn test -Dtest=ClassName` 또는 `-Dtest=ClassName#methodName`
- surefire는 항상 `-Djava.awt.headless=true`로 실행됨
- 서버 기동은 AI가 직접 하지 않는다. 실행은 사용자가 담당한다.
- 운영(prod) 서버/DB/LDAP 비밀번호는 사전 승인 전까지 건드리지 않는다.

## 로컬 Oracle (DB 필요 작업 전용)

- `mvn test` 게이트는 **DB 없이 통과**한다. `ConventionTest`는 소스 스캔, Service/Controller 테스트는 Mapper를 Mockito로 mock. 운영 DB 없이도 빌드·규약 검증 가능.
- 실제 Oracle이 필요한 작업(scaffold 타입 추론, 수동 연동 확인)만 아래 절차로 로컬 Oracle 기동:
  ```bash
  export SMS_DB_PASSWORD=oracle
  docker compose up -d --wait          # gvenzl/oracle-free:23-slim, 서비스명 SMS(PDB)
  ./docker/db-init.sh                  # SMS 유저 생성 + 로컬 EMP/DEP 픽스처 + db/oracle DDL/seed (1회, healthy 후)
  ```
- `db-init.sh`는 로컬 전용 EMP/DEP 픽스처(`docker/local-emp-dep-seed.sql`)와 `db/oracle/*.sql`을 적재한다. **운영 DB에는 절대 실행하지 않는다.**

## 환경 / Profile

- 기본 profile: `local`(`SPRING_PROFILES_ACTIVE` 미설정 시). 기본 포트 `8081`.
- profile별 차이는 **인증 방식뿐**. 권한 판단은 모든 환경에서 동일한 v3 권한 테이블 기준.
  - local → ID-only 인증(EMP 1건 확인, `EMP.ACT_YN = 'Y'` 필요)
  - dev/prod → LDAP 인증(LDAP 접속 정보는 현재 placeholder)
- 환경 변수: `SMS_DB_URL`, `SMS_DB_USERNAME`, `SMS_DB_PASSWORD`, `SMS_LDAP_MANAGER_PASSWORD`, `SMS_SCAFFOLD_DB_PLATFORM`, `SERVER_PORT`
- 메뉴/역할 source: `sms.menu.source=static|db`, `sms.role.source=static|db`. DB 실패 시 static 자동 대체 없음. 잘못된 조합(prod static, menu=db+role=static)은 `AuthSourceGuard`가 부팅 시 실패시킨다.

## 규칙 읽기 순서 (단일 기준: `docs/base/rules-index.md`)

1. `docs/base/README.md` — 전체 그림
2. `docs/base/rules-index.md` — 작업 유형별 읽을 문서 라우팅 표. **이 표에 있는 문서만 읽는다.** 한 작업에서 읽는 `docs/base` 문서는 4개 이하.
3. 작업 유형별 `.claude/rules/*.md`(path-scoped 짧은 실행 규칙) + 해당 `docs/base/*.md`(설계 근거)

`.claude/rules`와 `docs/base`가 충돌하면 `docs/base` 설계 원문 우선하고 `.claude/rules`를 즉시 갱신. 문서와 코드가 충돌하면 임의 선택하지 않고 보고. 새 문서는 반드시 README 문서 목록과 라우팅 표에 등록(미등록 문서는 없는 것).

## 핵심 도메인 규칙

- 사용자 기준 테이블은 `EMP`, 부서 기준은 `DEP`. `EMP` 기본키는 `(EMP_ID, DEP_ID)` 복합키. 사용자 식별·권한·감사로그·메뉴 권한을 `EMP_ID` 단독으로 설계하지 않는다.
- `EMP.PERM_*` 컬럼은 v3 권한 판단에 사용하지 않는다(legacy 참고값만).
- v3 권한 테이블: `TB_ROLE`, `TB_EMP_ROLE`, `TB_MENU`, `TB_MENU_AUTH`.
- 메뉴 seed는 v2 운영 메뉴와 동일한 구조(`v2-menu-baseline.md`). 메뉴 seed 변경 시 `StaticMenuSource`와 `db/oracle/02_menu_auth_seed.sql`을 함께 갱신.
- 개발자가 폐쇄망 DB에 테이블을 직접 만들 수 없으므로 DBA 전달용 DDL 문서를 반드시 유지한다.

## 코드 규약 (요약 — 상세는 `.claude/rules/project.md`, `mybatis-oracle.md`, `docs/base/*`)

- 계층: `controller → service → mapper interface → mapper XML → Oracle`
- JSON 응답: `ApiResponse<T>`. 목록은 `ApiResponse<PageResponseDTO<VO>>`(`PageResponseDTO.of(list, request, totalCount)`로만 생성).
- 조회 결과는 VO, 요청 파라미터는 DTO. 검색 DTO는 `PageRequestDTO` 상속. 수정/등록은 화이트리스트 `*UpdateRequestDTO`(VO를 `@RequestBody`로 받지 않음, 시스템/권한 필드 제외).
- 신규 도메인 코드·scaffold 생성물은 Lombok(`@Data`/`@RequiredArgsConstructor`). BASE 공통 코드(`dto/common`, `exception`, `util`, `auth`, `menu`)는 plain Java 유지 — 스타일 통일 목적 리팩토링 금지.
- 등록/수정 endpoint는 `/create`, `/update` 분리. 겸용 `/save`는 신규 코드에서 만들지 않는다.
- `@Transactional`은 Service 계층만. 조회는 `readOnly = true` 우선 검토.
- 업무 오류는 `CustomException(ErrorCode)`. 예외 → JSON 변환은 `GlobalExceptionHandler`에서만. URL/API 권한은 `MenuAuthInterceptor`가 담당, Controller에서 중복 검사 금지.
- MyBatis: `SELECT *` 금지, 컬럼-VO alias 정합, `OFFSET ... FETCH NEXT` 페이징, 결정적 `ORDER BY` 필수, count/목록 쿼리 동일 검색조건, update는 수정 컬럼만 + `UPDATE_DTTM = #{beforeUpdateDttm}` 낙관적 잠금(null-safe).
- Query Scaffold는 local 전용(`@Profile("local")` 유지), 메뉴에 등록하지 않는다.
- `ConventionTest`가 DTO 상속·`/save` 금지·`SELECT *` 금지·정렬 필수·Lombok 규약을 자동 검증. 규약 변경 시 함께 갱신.
- 운영 중 바뀔 값(타임아웃, 경로, 한도)은 `application*.yml`로 외부화. 매직넘버/스트링은 상수, 같은 로직 2곳 이상이면 메서드 추출, 파일 300줄 초과 시 분리 검토.

## 완료 기준 (코드 변경 시)

- `mvn test` 성공
- `mvn -DskipTests package` 성공
- 관련 문서 갱신
- local/dev/prod profile별 설정 검토

문서만 변경한 작업은 Maven 실행 대상이 아니다. 단, 문서가 코드 동작을 바꾸도록 지시하면 코드 반영 전까지 부분 완료. 테스트/빌드 미실행 상태는 완료가 아니라 부분 완료로 보고. 같은 오류 3회 반복 시 원문 오류·시도한 3가지·추정 원인을 보고한다.

## 커밋 규칙

- 메시지 형식: `feat|fix|refactor|docs|chore(scope): 한 줄 요약`
- 커밋 전 `git status --short`로 변경 파일 확인. `target/`, `.env.local`, 비밀값이 staged에 없는지 검사.
- 커밋 전 변경 파일 목록과 메시지 초안을 사용자에게 공유하고 **승인 후** 커밋.
- 기본 단위: 화면 1개 = 커밋 1개(scaffold 산출물 + 테스트 + 메뉴 SQL). BASE 공통 코드 수정과 화면 생성을 한 커밋에 섞지 않는다. BASE 공통 코드 수정 커밋은 `mvn test` PASS가 전제.

## 금지

- 실패를 fallback으로 우회하지 않는다.
- 인증/권한 오류를 try-catch로 삼키지 않는다.
- 실제 DB 구조 확인 없이 컬럼명을 확정하지 않는다.
- 지시받지 않은 패턴/라이브러리/의존성을 임의로 도입하지 않는다. 필요하면 먼저 승인.
- 운영(prod) 서버/DB 작업을 사전 승인 없이 진행하지 않는다.
- 운영/LDAP/DB 비밀번호를 문서나 코드에 기록하지 않는다.
- `EMP.PERM_*`를 신규 권한 로직의 기준으로 사용하지 않는다.
