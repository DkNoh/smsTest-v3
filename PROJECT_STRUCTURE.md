# smsTest-v3 프로젝트 구조 분석

## 1. 프로젝트 개요

| 항목 | 내용 |
|---|---|
| **프로젝트명** | sms-project-v3 |
| **기술 스택** | Spring Boot 3.3.0 + Java 21 + Oracle + MyBatis + Thymeleaf |
| **패키징** | WAR (내장 Tomcat / 외부 Tomcat 양쪽 지원) |
| **목적** | 폐쇄망 SMS/LMS/알림톡 대량 발송 및 이력 관리 시스템 v3 BASE PROJECT |
| **인증** | local: ID-only / dev, prod: LDAP |
| **권한** | v3 신규 권한 모델 (TB_ROLE, TB_EMP_ROLE, TB_MENU, TB_MENU_AUTH) |
| **그룹/도메인** | com.example / sms |

---

## 2. 디렉토리 구조

```
smsTest-v3/
├── pom.xml                          # Maven 빌드 설정
├── AGENTS.md                        # Codex 작업 규칙
├── CLAUDE.md                        # Claude Code 작업 규칙
├── .claude/rules/                   # AI 에이전트 도메인별 규칙 8개
├── docs/base/                       # 설계/정책/구현 문서 20개+
├── db/oracle/                       # DDL + Seed SQL 3개
├── src/main/java/com/example/sms/
│   ├── SmsV3Application.java        # @SpringBootApplication
│   ├── ServletInitializer.java      # WAR 외부 Tomcat 지원
│   ├── config/                      # 설정 클래스 (5개)
│   ├── auth/                        # 인증 도메인 (4개)
│   ├── controller/                  # 컨트롤러 (5개)
│   ├── service/                     # 서비스 레이어 (10개+)
│   ├── mapper/                      # MyBatis Mapper 인터페이스 (7개)
│   ├── dto/                         # 요청/응답 DTO (6개)
│   ├── vo/                          # View Object (7개)
│   ├── util/                        # 유틸리티 (2개)
│   ├── aop/                         # AOP (1개)
│   ├── annotation/                  # 애노테이션 (1개)
│   └── exception/                   # 예외 처리 (2개)
├── src/main/resources/
│   ├── application.yml              # 기본 설정
│   ├── application-local.yml        # local profile
│   ├── application-dev.yml          # dev profile
│   ├── application-prod.yml         # prod profile
│   ├── mapper/                      # MyBatis XML 매퍼
│   ├── templates/                   # Thymeleaf 템플릿
│   └── static/                      # 정적 파일
└── src/test/java/com/example/sms/   # 테스트 (15개)
```

---

## 3. pom.xml 의존성

### 핵심 의존성

| 의존성 | 버전 | 용도 |
|---|---|---|
| spring-boot-starter-web | 3.3.0 | REST + MVC |
| spring-boot-starter-thymeleaf | - | 서버사이드 템플릿 |
| spring-boot-starter-security | - | Spring Security 인증/권한 |
| spring-boot-starter-validation | - | Bean Validation |
| spring-boot-starter-aop | - | AOP (@PrivacyLog) |
| spring-boot-starter-data-ldap | - | LDAP 인증 |
| mybatis-spring-boot-starter | 3.0.3 | MyBatis ORM |
| ojdbc11 | - | Oracle JDBC |
| poi-ooxml | 5.2.3 | Excel 생성 (SXSSF) |
| jsqlparser | 4.9 | SQL 파싱 (Query Scaffold) |
| lombok | provided | 코드 줄임 |
| thymeleaf-layout-dialect | - | 레이아웃 템플릿 |

---

## 4. application.yml 설정

### 기본 설정 (application.yml)

```yaml
spring:
  application:
    name: sms-project-v3
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}   # 기본 local
  thymeleaf:
    cache: false

mybatis:
  mapper-locations: classpath:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true

server:
  port: ${SERVER_PORT:8081}
  servlet:
    session:
      tracking-modes: cookie
```

### 프로필별 차이

| 설정 | local | dev | prod |
|---|---|---|---|
| 인증 방식 | ID-only | LDAP | LDAP |
| 메뉴 source | static 또는 db | db | db |
| 역할 source | static 또는 db | db | db |
| Scaffold | 활성화 | 비활성화 | 비활성화 |
| 서버 포트 | 8081 | 8081 | 8081 |

---

## 5. 인증/권한 아키텍처

### 5.1 인증 흐름

```
[Client] → [Spring Security FilterChain]
                │
                ├── formLogin (/login) → AuthenticationProvider
                │       │
                │       ├── LocalIdOnlyAuthenticationProvider  (local profile)
                │       │       └── ActiveEmployeeResolver.resolveSingleActiveEmployee()
                │       │       └── EmployeeRoleService.getActiveRoleCodes()
                │       │
                │       └── LdapAuthenticationProvider         (dev/prod profile)
                │               └── BindAuthenticator (LDAP 바인딩)
                │               └── LdapEmployeeContextMapper
                │                       └── ActiveEmployeeResolver.resolveSingleActiveEmployee()
                │                       └── EmployeeRoleService.getActiveRoleCodes()
                │
                └── [MenuAuthInterceptor] → MenuAuthService.checkAccess()
                        │
                        ├── URL 정확 일치 → READ 권한 검증
                        ├── URL suffix 매칭 → CREATE/UPDATE/DELETE 등 검증
                        └── 미매칭 → 403 ACCESS_DENIED
```

### 5.2 인증 클래스 상세

#### SmsV3Application.java
- `@SpringBootApplication` 메인 진입점
- `main()`에서 `SpringApplication.run()` 호출

#### SecurityConfig.java
- `@EnableWebSecurity` + `@Configuration`
- CSRF disable (formLogin 기반)
- `/login`, `/error` → permitAll
- 정적 리소스 (`/css/**`, `/js/**`, `/lib/**`, `/vendor/**`, `/img/**`) → permitAll
- 나머지 → authenticated
- formLogin: `/login` 페이지, `empId`/`password` 파라미터
- logout: `/logout`, 세션 무효화, JSESSIONID 쿠키 삭제
- `AuthenticationProvider` 리스트를 모두 `http.authenticationProvider()`에 등록
- `PasswordEncoder` → BCrypt

#### WebMvcConfig.java
- `@Configuration` + `WebMvcConfigurer`
- `MenuAuthInterceptor`를 `/**`에 등록
- 제외 경로: `/`, `/login`, `/logout`, `/error`, 정적 리소스, `/api/common-code/**`
- `sms.menu.auth.exclude-paths` 설정으로 추가 제외 가능

#### MenuAuthInterceptor.java
- `HandlerInterceptor` 구현
- `SecurityContextHolder`에서 `SmsUserPrincipal` 추출
- `MenuAuthService.checkAccess(path, roleCodes)` 호출
- 미인증 요청은 Spring Security가 1차 차단

### 5.3 인증 Provider 상세

#### LocalIdOnlyAuthenticationProvider.java (local profile)
- `@Component` + `@Profile("local")`
- `AuthenticationProvider` 구현
- `authenticate()`:
  1. `ActiveEmployeeResolver.resolveSingleActiveEmployee(empId)` → `LoginEmployeeVO`
  2. `EmployeeRoleService.getActiveRoleCodes(empId, depId)` → 역할 목록
  3. `SmsUserPrincipal` 생성 → `UsernamePasswordAuthenticationToken` 반환
- 비밀번호 검증 없이 EMP_ID만으로 인증 (local 전용)

#### LdapAuthenticationConfig.java (dev/prod profile)
- `@Configuration` + `@Profile({"dev", "prod"})`
- `@EnableConfigurationProperties(SmsLdapProperties.class)`
- `AuthenticationProvider` Bean 생성:
  1. `DefaultSpringSecurityContextSource` (LDAP URL + BaseDN)
  2. `BindAuthenticator` (LDAP 바인딩 인증)
  3. `DefaultLdapAuthoritiesPopulator`
  4. `LdapEmployeeContextMapper` (LDAP → EMP 매핑)

#### SmsLdapProperties.java
- LDAP 연결 설정 프로퍼티 클래스
- `url`, `baseDn`, `managerDn`, `managerPassword`
- `userSearchBase`, `userSearchFilter`

#### ActiveEmployeeResolver.java
- `@Component`
- `LoginEmployeeMapper.selectActiveEmployeesByEmpId(empId)` 호출
- 0건 → `BadCredentialsException("활성 사용자 정보를 찾을 수 없습니다.")`
- 2건 이상 → `BadCredentialsException("동일 사번에 활성 부서가 여러 건입니다.")`
- 정확 1건만 반환 (EMP_ID + DEP_ID 복합키 제약)

#### LdapEmployeeContextMapper.java
- `UserDetailsContextMapper` 구현
- `mapUserFromContext()`:
  1. `ActiveEmployeeResolver`로 EMP 조회
  2. `EmployeeRoleService`로 역할 조회
  3. `SmsUserPrincipal` 생성
- `mapUserToContext()` → `UnsupportedOperationException` (LDAP 쓰기는 지원하지 않음)

#### SmsUserPrincipal.java (UserDetails 구현체)
- `empId`, `depId`, `empNm`, `depNm`, `roleCodes`
- `getAuthorities()` → roleCodes를 `SimpleGrantedAuthority`로 변환
- `getPassword()` → 빈 문자열 (local 인증용)
- `getUsername()` → empId 반환

### 5.4 권한 모델 (v3 신규)

#### MenuPermission.java
```
READ, CREATE, UPDATE, DELETE, APPROVE, CANCEL, DOWNLOAD, MASK_VIEW
```
TB_MENU_AUTH의 CAN_* 컬럼과 1:1 대응

#### MenuAuthProvider.java (ABC 인터페이스)
```java
Set<MenuPermission> getPermissions(String menuUrl, List<String> roleCodes);
```
- 메뉴 URL + 역할 목록으로 보유 권한 Set 반환
- 권한 없으면 빈 Set 반환

#### StaticMenuAuthProvider.java (sms.menu.source=static)
- `@ConditionalOnProperty(name = "sms.menu.source", havingValue = "static")`
- 메뉴 테이블 생성 전 local 화면 검증용
- baseline에 있는 메뉴 URL에는 **모든 권한** 부여
- 최종 권한 검증은 반드시 DB source에서 확인

#### DbMenuAuthProvider.java (sms.menu.source=db)
- `@ConditionalOnProperty(name = "sms.menu.source", havingValue = "db")`
- `MenuAuthMapper.selectMenuPermissions(menuUrl, roleCodes)` 호출
- `MenuAuthVO`의 CAN_* 필드를 `MenuPermission` Set으로 매핑
- "Y"인 권한만 추가

#### MenuAuthService.java
- `checkAccess(path, roleCodes)` 핵심 판정 로직
- 판정 순서:
  1. 요청 URL이 메뉴 URL과 **정확히 일치** → READ 권한 검증
  2. 일치하는 메뉴 없으면 **URL suffix** 떼고 부모 화면 기준으로 액션 권한 검증
  3. 어느 메뉴에도 연결되지 않는 URL → 403 ACCESS_DENIED
- Suffix → 권한 매핑:
  - `/data`, `/search`, `/detail`, `/tree` → READ
  - `/create`, `/register` → CREATE
  - `/update` → UPDATE
  - `/save` → CREATE + UPDATE (겸용)
  - `/delete` → DELETE
  - `/approve`, `/reject` → APPROVE
  - `/cancel` → CANCEL
  - `/excel`, `/download`, `/export` → DOWNLOAD
  - `/unmask` → MASK_VIEW

#### MenuProvider.java (ABC 인터페이스)
```java
List<MenuItemVO> getMenuTree(List<String> roleCodes);
```
역할 기반 메뉴 트리 반환

#### StaticMenuProvider.java (sms.menu.source=static)
- `@ConditionalOnProperty(name = "sms.menu.source", havingValue = "static")`
- 하드코딩된 메뉴 baseline (v2 운영 메뉴와 동일 구조)
- 5개 그룹: G_BASIC, G_SMS_SEARCH, G_CAMPAIGN, G_SYSTEM, G_ACCOUNT, G_STATISTICS
- 28개 메뉴 항목 (Group + Menu)

#### DbMenuProvider.java (sms.menu.source=db)
- `@ConditionalOnProperty(name = "sms.menu.source", havingValue = "db")`
- `MenuMapper.selectReadableMenus(roleCodes)` → 평면 메뉴 목록
- `MenuTreeBuilder.build()` → 트리 구조로 변환

#### RoleProvider.java (ABC 인터페이스)
```java
List<String> getActiveRoleCodes(String empId, String depId);
```

#### DbRoleProvider.java (sms.role.source=db)
- `@ConditionalOnProperty(name = "sms.role.source", havingValue = "db")`
- `EmployeeRoleMapper.selectRoleCodes(empId, depId)` 호출

#### StaticRoleProvider.java (sms.role.source=static)
- `@ConditionalOnProperty(name = "sms.role.source", havingValue = "static")`
- `sms.role.static-default` 설정값을 단일 역할로 반환

#### EmployeeRoleService.java
- `RoleProvider` 래퍼
- 역할이 없으면 `BadCredentialsException("No active role assigned to employee.")`

#### MenuService.java
- `MenuProvider` 래퍼
- `GlobalModelAdvice`에서 메뉴 트리 렌더링용

#### MenuTreeBuilder.java
- `@Component`
- 평면 메뉴 목록 → 트리 구조 변환
- `menuId` 중복 검증, 부모-자식 참조 검증

### 5.5 EMP/DEP 기반 사용자 모델

#### LoginEmployeeVO.java
```
empId, depId, empNm, depNm, actYn, depActYn
```
- `EMP_ID` + `DEP_ID` 복합키가 기본 식별자
- `EMP.PERM_*`는 v3 권한 판단에 **사용하지 않음** (legacy 참고용)

#### MenuItemVO.java
```
menuId, parentMenuId, menuNm, menuUrl, menuLevel, sortOrd,
menuType, iconNm, displayYn, useYn, systemYn, children[]
```
- `menuType`: "G"(그룹), "M"(메뉴)
- `children`: 자식 메뉴 (재귀 구조)

---

## 6. 컨트롤러 구조

### 6.1 HomeController.java
- `@GetMapping("/")` → `index.html`
- `activeProfiles`, `menuSource`, `roleSource`를 모델에 전달
- 현재 프로필과 메뉴/역할 소스 표시

### 6.2 LoginController.java
- `@GetMapping("/login")` → `login.html`
- `sms.auth.mode` 프로퍼티로 `localMode` 플래그 전달
- local 모드일 때 ID-only 로그인 UI 표시

### 6.3 GlobalModelAdvice.java
- `@ControllerAdvice` + `@ModelAttribute`
- 모든 화면에 공통 모델 주입:
  - `user` → `SmsUserPrincipal`
  - `menus` → `MenuService.getMenuTree(roleCodes)` (사이드바 메뉴)

### 6.4 SmsHistoryController.java (`/sms/history`)
| HTTP | URL | 반환 | 설명 |
|---|---|---|---|
| GET | `/sms/history` | `sms/history.html` | 목록 페이지 |
| GET | `/sms/history/data` | `ApiResponse<PageResponseDTO<SmsHistoryVO>>` | 페이지 목록 조회 (JSON) |
| POST | `/sms/history/create` | `ApiResponse<String>` | 등록 |
| POST | `/sms/history/update` | `ApiResponse<String>` | 수정 |
| POST | `/sms/history/delete` | `ApiResponse<String>` | 삭제 |
| GET | `/sms/history/excel` | Excel 다운로드 | 엑셀 내보내기 |

### 6.5 CommonCodeApiController.java (`/api/common-code`)
- `@RestController` (Thymeleaf 렌더링 없음, JSON 전용)
- `@GetMapping("/{codeType}")` → 콤보/자동완성 데이터
- `codeType`: "dept" (부서), "role" (역할)
- 메뉴 권한 Interceptor 검증 **대상에서 제외** (WebMvcConfig 공통 제외 경로)
- Spring Security 인증(로그인)은 적용됨

### 6.6 ScaffoldController.java (`/system/scaffold`, local 전용)
- `@Profile("local")` → local 개발 환경에서만 접근 가능
- `@PostMapping("/generate")` → QuerySpec 기반 화면 코드 생성
- 메뉴에 등록되지 않음 (exclude-paths로 접근 연다)

---

## 7. 서비스 레이어 구조

### 7.1 SmsHistoryService.java
| 메서드 | 트랜잭션 | 설명 |
|---|---|---|
| `search(request)` | `@Transactional(readOnly=true)` | 페이지 목록 조회 (count + selectList) |
| `create(request)` | `@Transactional` | INSERT |
| `update(request)` | `@Transactional` | UPDATE + 낙관적 잠금 체크 (updated==0 → UPDATE_CONFLICT) |
| `delete(id)` | `@Transactional` | DELETE |
| `downloadExcel(request, response)` | `@Transactional(readOnly=true)` | Excel 다운로드 (Map 사용, 동적 컬럼 예외) |

### 7.2 CommonCodeService.java
- `getCommonCodes(codeType, keyword)`
- "dept" → `CommonCodeMapper.selectDepartments()`
- "role" → `CommonCodeMapper.selectRoles()`
- 미지원 타입 → `CustomException(UNSUPPORTED_CODE_TYPE)`

### 7.3 AuditLogService.java
- `saveLog(logVO)` → `PrivacyAuditLogMapper.insertAuditLog()`
- `@Transactional`
- **감사 로그 저장 실패는 삼키지 않고 전파** (v2와 다른 v3 확정 동작)

### 7.4 ScaffoldService.java (local 전용)
- `generate(request)` → QuerySpec에서 13개 파일 코드 생성
- 생성 파일:
  1. `*SearchRequestDTO.java`
  2. `*UpdateRequestDTO.java` (옵션)
  3. `*VO.java`
  4. `*Mapper.java`
  5. `*Mapper.xml`
  6. `*Service.java`
  7. `*Controller.java`
  8. `*ServiceTest.java`
  9. `*ControllerTest.java`
  10. `*.html`
  11. `*.js`
  12. `메뉴등록.sql`
- 파일을 직접 쓰지 않음. 산출물은 사람이 검토 후 반영

### 7.5 스캐폴드 템플릿 클래스들 (scaffold 하위 패키지)
- `ColumnTypeInferrer` — SQL 컬럼 타입 추론
- `QueryColumnExtractor` — rawQuery에서 SELECT 컬럼/검색 변수 추출
- `ScaffoldModel` — 생성 모델 데이터 구조
- `DtoTemplate`, `UpdateRequestDtoTemplate`, `VoTemplate`
- `MapperInterfaceTemplate`, `MapperXmlTemplate`
- `ServiceTemplate`, `ControllerTemplate`
- `ServiceTestTemplate`, `ControllerTestTemplate`
- `HtmlTemplate`, `JsTemplate`
- `MenuSqlTemplate`

---

## 8. MyBatis 매퍼 구조

### 8.1 Mapper 인터페이스

| Mapper | 메서드 | 설명 |
|---|---|---|
| `SmsHistoryMapper` | `count()`, `selectList()`, `insert()`, `update()`, `delete()`, `selectListForExcel()` | SMS 이력 CRUD |
| `LoginEmployeeMapper` | `selectActiveEmployeesByEmpId()` | 활성 EMP 조회 (인증용) |
| `MenuMapper` | `selectReadableMenus()` | 읽기 가능한 메뉴 평면 목록 |
| `MenuAuthMapper` | `selectMenuPermissions()` | 메뉴 권한 (CAN_* 컬럼 MAX 집계) |
| `EmployeeRoleMapper` | `selectRoleCodes()` | EMP_ID+DEP_ID 기반 역할 목록 |
| `CommonCodeMapper` | `selectDepartments()`, `selectRoles()` | 공통코드 조회 |
| `PrivacyAuditLogMapper` | `insertAuditLog()` | 감사 로그 INSERT |

### 8.2 XML 매퍼 위치
- `src/main/resources/mapper/auth/` — LoginEmployeeMapper.xml
- `src/main/resources/mapper/menu/` — MenuMapper.xml, MenuAuthMapper.xml, EmployeeRoleMapper.xml
- `src/main/resources/mapper/sms/` — SmsHistoryMapper.xml
- `src/main/resources/mapper/system/` — CommonCodeMapper.xml, PrivacyAuditLogMapper.xml

---

## 9. DTO/VO 구조

### 9.1 공통 DTO

#### PageRequestDTO.java
- `page` (기본 1), `size` (기본 10, 최대 100), `keyword`, `searchType`
- `getOffset()` → `(page - 1) * size`
- `validate()` → page < 1 → 1, size < 1 → 10, size > 100 → 100

#### PageResponseDTO.java
- `contents`, `page`, `size`, `totalCount`, `totalPages`, `hasNext`, `hasPrev`
- 정적 팩토리 `of(list, request, totalCount)`만 생성 경로

#### ApiResponse.java
- `timestamp`, `code`, `message`, `data`
- `success(data)`, `success(message, data)`, `error(code, message)`

### 9.2 도메인 DTO

#### SmsHistorySearchRequestDTO.java
- `PageRequestDTO` 상속
- `startDt` — 시작일 검색

#### SmsHistoryUpdateRequestDTO.java
- `id` — PK (WHERE 조건, TODO: 실제 컬럼명 교체)
- `sentAt`, `receiverNo`, `sendType` — 수정 가능 필드
- `beforeUpdateDttm` — 낙관적 잠금용

#### ScaffoldRequestDTO.java
- `moduleName`, `domainId`, `domainClass`, `domainName` (모두 @NotBlank)
- `rawQuery` — 원본 SQL (QuerySpec)
- `orderBy` — 결정적 정렬 컬럼 (@NotBlank)
- `includeCreateUpdate`, `includeExcel`, `includePrivacy` — 옵션 플래그

### 9.3 VO (View Object)

| VO | 필드 | 용도 |
|---|---|---|
| `LoginEmployeeVO` | empId, depId, empNm, depNm, actYn, depActYn | 로그인 EMP 정보 |
| `SmsHistoryVO` | rowNum, sentAt, receiverNo, sendType | SMS 이력 조회 |
| `MenuAuthVO` | canRead, canCreate, canUpdate, canDelete, canApprove, canCancel, canDownload, canMaskView | 메뉴 권한 (String "Y"/"N") |
| `MenuItemVO` | menuId, parentMenuId, menuNm, menuUrl, menuLevel, sortOrd, menuType, iconNm, displayYn, useYn, systemYn, children[] | 메뉴 트리 |
| `PrivacyAuditLogVO` | empId, depId, executorIp, requestUrl, actionType, targetData | 감사 로그 |
| `CommonCodeVO` | code, name | 공통코드 콤보 항목 |

---

## 10. 예외 처리 구조

### 10.1 CustomException.java
- `RuntimeException` 상속
- `ErrorCode` 포함
- Service 레이어에서 예측 가능한 오류 던짐

### 10.2 ErrorCode.java (에러 코드 enum)

| 코드 | HTTP 상태 | 메시지 |
|---|---|---|
| C001 | 400 BAD_REQUEST | 입력값이 올바르지 않습니다 |
| C002 | 500 INTERNAL_SERVER_ERROR | 서버 내부 오류가 발생했습니다 |
| C003 | 400 BAD_REQUEST | 지원하지 않는 공통코드 타입입니다 |
| C004 | 409 CONFLICT | 다른 사용자가 먼저 수정했거나 대상 데이터가 없습니다 |
| A001 | 401 UNAUTHORIZED | 인증되지 않은 사용자입니다 |
| A002 | 403 FORBIDDEN | 해당 기능에 대한 접근 권한이 없습니다 |
| U001 | 404 NOT_FOUND | 사용자를 찾을 수 없습니다 |
| U002 | 409 CONFLICT | 이미 존재하는 사용자입니다 |
| M001 | 409 CONFLICT | 이미 같은 URL을 사용하는 메뉴가 존재합니다 |

---

## 11. AOP / 감사 로그 구조

### 11.1 PrivacyLog.java (애노테이션)
- `action()` — 행위 타입 (예: "CREATE", "UPDATE", "DELETE")

### 11.2 PrivacyLogAspect.java
- `@Aspect` + `@Component`
- `@Around("@annotation(privacyLog)")` — `@PrivacyLog` 붙은 메서드 감지
- 동작:
  1. `request.getRequestURI()` → `requestUrl`
  2. `principal.getEmpId()` + `principal.getDepId()` → 행위자
  3. `X-Forwarded-For` 또는 `remoteAddr` → `executorIp`
  4. 파라미터 직렬화 → `MaskingUtil.maskPrivacyInText()` → 마스킹
  5. 마스킹 결과 490자 제한 (초과 시 `...` truncation)
  6. `AuditLogService.saveLog()` → DB 저장
- **감사 로그 저장 실패는 전파** (본 업무도 실패)

---

## 12. 유틸리티

### 12.1 MaskingUtil.java
- `maskName("홍길동")` → `홍*동`
- `maskPhone("01012345678")` → `010-****-5678`
- `maskRrn("9001011234567")` → `900101-1******`
- `maskCard("1234567812345678")` → `1234-****-****-5678`
- `maskPrivacyInText(json)` — JSON 직렬화 결과에서 RRN/전화번호 자동 마스킹

### 12.2 ExcelUtil.java
- `downloadExcel(response, fileName, headers, dataList, keys)`
- `SXSSFWorkbook` (100행 단위 플러시) → OOM 방지
- 헤더 스타일: 회색 배경, 볼드, 중앙 정렬
- `Content-Disposition: attachment` → 다운로드

---

## 13. DB 스키마 (db/oracle/)

| 파일 | 내용 |
|---|---|
| `01_menu_auth_schema.sql` | TB_MENU_AUTH, TB_MENU, TB_ROLE, TB_EMP_ROLE 등 신규 권한 테이블 DDL |
| `02_menu_auth_seed.sql` | v2 운영 메뉴 seed 데이터 |
| `03_privacy_audit_log_schema.sql` | TB_PRIVACY_AUDIT_LOG DDL |

### v3 권한 테이블 구조
- `TB_ROLE` — 역할 정의
- `TB_EMP_ROLE` — EMP_ID + DEP_ID 복합키로 EMP 연결
- `TB_MENU` — 메뉴 정의 (v2 운영 메뉴와 동일 구조)
- `TB_MENU_AUTH` — 역할 × 메뉴 × 권한 (CAN_READ, CAN_CREATE, ...)

---

## 14. 테스트 구조

| 테스트 클래스 | 내용 |
|---|---|
| `ConventionTest.java` | 프로젝트 컨벤션 검증 |
| `PrivacyLogAspectTest.java` | @PrivacyLog AOP 동작 검증 |
| `GlobalExceptionHandlerTest.java` | 예외 → HTTP 상태 코드 매핑 |
| `MenuAuthServiceTest.java` | URL → 권한 판정 로직 |
| `StaticMenuAuthProviderTest.java` | static 메뉴 권한 검증 |
| `AuditLogServiceTest.java` | 감사 로그 저장 |
| `CommonCodeServiceTest.java` | 공통코드 조회 |
| `ColumnTypeInferrerTest.java` | SQL 컬럼 타입 추론 |
| `MapperInterfaceTemplateTest.java` | 스캐폴드 매퍼 템플릿 |
| `ScaffoldTemplateTest.java` | 스캐폴드 전체 템플릿 |
| `PageRequestDTOTest.java` | 페이지 요청 DTO |
| `PageResponseDTOTest.java` | 페이지 응답 DTO |
| `ApiResponseTest.java` | 공통 응답 DTO |
| `MaskingUtilTest.java` | 마스킹 유틸리티 |
| `ExcelUtilTest.java` | Excel 유틸리티 |

---

## 15. 핵심 설계 원칙 요약

1. **EMP_ID + DEP_ID 복합키**가 기본 식별자. EMP_ID 단독 식별 X
2. **EMP.PERM_*는 v3 권한 판단에 사용하지 않음** (legacy 참고용)
3. **인증은 profile별**, **권한은 공통 v3 테이블**로 통일
4. **MenuAuthProvider / RoleProvider / MenuProvider**는 ABC 패턴 + `@ConditionalOnProperty`로 static/db 전환
5. **Query Scaffold**는 local 전용 개발 도구. rawQuery에서 코드 자동 생성 (파일 쓰지 않음, 사람이 검토)
6. **@PrivacyLog AOP**는 모든 개인정보 접근을 감사. 저장 실패는 전파 (삼키지 않음)
7. **MaskingUtil**은 RRN/전화번호/이름/카드번호 마스킹
8. **DBA 전달용 DDL 문서**를 반드시 유지 (개발자가 직접 테이블 생성 불가)
9. **메뉴 source**는 `sms.menu.source=static|db` 명시적 선택. DB 실패 시 static으로 자동 대체 X
