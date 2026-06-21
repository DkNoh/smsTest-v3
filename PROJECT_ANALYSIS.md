STAGE_1_DONE

## 1. 기술 스택 및 구조
- 구조 스캔 범위: depth 3 이내, 생성물/의존성 디렉터리 제외.
- 주요 프로젝트 루트: `/Users/dk/Work/smsTest-v3`.
- 상위 구조에서 확인된 주요 디렉터리/파일:
  - `src/main/java`, `src/main/resources`, `src/test/java`, `src/test/resources`
  - `db/oracle`
  - `docs/base`
  - `docker`
  - `AGENTS.md`, `CLAUDE.md`, `.claude/rules`, `.gitignore`, `.vscode`
- Maven 프로젝트:
  - `artifactId`: `sms-project-v3`
  - `version`: `0.0.1-SNAPSHOT`
  - `packaging`: `war`
  - Java: `21`
- Spring Boot:
  - Spring Boot starter parent `3.3.0`
  - `spring-boot-starter-web`
  - `spring-boot-starter-thymeleaf`
  - `spring-boot-starter-security`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-actuator`
  - `spring-boot-starter-aop`
  - `spring-boot-starter-tomcat`
- MyBatis/Oracle:
  - `mybatis-spring-boot-starter` `3.0.3`
  - `com.oracle.database.jdbc:ojdbc11` runtime
- 기타 의존성:
  - `thymeleaf-layout-dialect`
  - `thymeleaf-extras-springsecurity6`
  - `spring-security-ldap`
  - Apache POI `poi-ooxml` `5.2.3`
  - JSqlParser `4.9`
  - Lombok
  - Spring Boot test / Spring Security test
- README 핵심 요약 (`docs/base/README.md`):
  - v3는 화면을 많이 만드는 프로젝트가 아니라 폐쇄망에서 반복 생성/검증 가능한 BASE PROJECT를 만드는 것이 목표.
  - local ID-only 인증, dev/prod LDAP 인증 분리.
  - 권한 판단은 profile과 무관하게 v3 권한 모델 사용.
  - 사용자/부서 모델은 운영의 `EMP`, `DEP`를 기준으로 사용.
  - `EMP.PERM_*`는 v3 권한 판단에 사용하지 않음.
  - v3 권한은 `TB_ROLE`, `TB_EMP_ROLE`, `TB_MENU`, `TB_MENU_AUTH`로 분리.
  - 실제 업무 화면 생성은 Query Scaffold와 `screen-generation-guide.md`를 통해 진행.

## 2. 모듈/기능 인벤토리
### 2.1 Controller / RestController
| 클래스 | 패키지 | 파일 | 기능 분류 |
|---|---|---|---|
| `HomeController` | `com.scbk.sms.controller` | `src/main/java/com/scbk/sms/controller/HomeController.java` | 홈 화면 진입 |
| `LoginController` | `com.scbk.sms.controller` | `src/main/java/com/scbk/sms/controller/LoginController.java` | 로그인 화면 |
| `SmsHistoryController` | `com.scbk.sms.controller.sms` | `src/main/java/com/scbk/sms/controller/sms/SmsHistoryController.java` | SMS/LMS 발신 이력 조회 화면 |
| `CommonCodeApiController` | `com.scbk.sms.controller.system` | `src/main/java/com/scbk/sms/controller/system/CommonCodeApiController.java` | 공통코드 REST API |
| `MenuTreeController` | `com.scbk.sms.controller.system` | `src/main/java/com/scbk/sms/controller/system/MenuTreeController.java` | 메뉴 트리 화면 |
| `ScaffoldController` | `com.scbk.sms.controller.system` | `src/main/java/com/scbk/sms/controller/system/ScaffoldController.java` | 스키폴더/스크린 생성 관련 컨트롤러 |

### 2.2 Service
| 클래스 | 패키지 | 파일 | 기능 분류 |
|---|---|---|---|
| `SmsHistoryService` | `com.scbk.sms.service.sms` | `src/main/java/com/scbk/sms/service/sms/SmsHistoryService.java` | SMS/LMS 발신 이력 서비스 |
| `ScaffoldService` | `com.scbk.sms.service.system` | `src/main/java/com/scbk/sms/service/system/ScaffoldService.java` | 스키폴더/스크린 생성 서비스 |
| `CommonCodeService` | `com.scbk.sms.service.system` | `src/main/java/com/scbk/sms/service/system/CommonCodeService.java` | 공통코드 서비스 |
| `AuditLogService` | `com.scbk.sms.service.system` | `src/main/java/com/scbk/sms/service/system/AuditLogService.java` | 감사로그/마스킹 관련 서비스 |
| `DbMenuSource` | `com.scbk.sms.service.menu` | `src/main/java/com/scbk/sms/service/menu/DbMenuSource.java` | DB 기반 메뉴 소스 |
| `DbRoleProvider` | `com.scbk.sms.service.menu` | `src/main/java/com/scbk/sms/service/menu/DbRoleProvider.java` | DB 기반 역할 제공 |
| `EmployeeRoleService` | `com.scbk.sms.service.menu` | `src/main/java/com/scbk/sms/service/menu/EmployeeRoleService.java` | 직원/역할 서비스 |
| `MenuAuthService` | `com.scbk.sms.service.menu` | `src/main/java/com/scbk/sms/service/menu/MenuAuthService.java` | 메뉴 권한 서비스 |
| `StaticMenuSource` | `com.scbk.sms.service.menu` | `src/main/java/com/scbk/sms/service/menu/StaticMenuSource.java` | static 메뉴 소스 |
| `StaticRoleProvider` | `com.scbk.sms.service.menu` | `src/main/java/com/scbk/sms/service/menu/StaticRoleProvider.java` | static 역할 제공 |

### 2.3 Mapper
| 클래스 | 패키지 | 파일 | 기능 분류 |
|---|---|---|---|
| `SmsHistoryMapper` | `com.scbk.sms.mapper.sms` | `src/main/java/com/scbk/sms/mapper/sms/SmsHistoryMapper.java` | SMS/LMS 발신 이력 DB 접근 |
| `LoginEmployeeMapper` | `com.scbk.sms.mapper.auth` | `src/main/java/com/scbk/sms/mapper/auth/LoginEmployeeMapper.java` | 로그인 직원 조회 |
| `PrivacyAuditLogMapper` | `com.scbk.sms.mapper.system` | `src/main/java/com/scbk/sms/mapper/system/PrivacyAuditLogMapper.java` | 개인정보 감사로그 DB 접근 |
| `CommonCodeMapper` | `com.scbk.sms.mapper.system` | `src/main/java/com/scbk/sms/mapper/system/CommonCodeMapper.java` | 공통코드 DB 접근 |
| `MenuAuthMapper` | `com.scbk.sms.mapper.menu` | `src/main/java/com/scbk/sms/mapper/menu/MenuAuthMapper.java` | 메뉴 권한 DB 접근 |
| `EmployeeRoleMapper` | `com.scbk.sms.mapper.menu` | `src/main/java/com/scbk/sms/mapper/menu/EmployeeRoleMapper.java` | 직원/역할 DB 접근 |
| `MenuMapper` | `com.scbk.sms.mapper.menu` | `src/main/java/com/scbk/sms/mapper/menu/MenuMapper.java` | 메뉴 DB 접근 |

### 2.4 Repository
- grep 결과에서 `@Repository`가 붙은 클래스는 확인되지 않음.

## 3. 모듈별 기능 상세
### 3.1 auth 모듈
#### HomeController (`src/main/java/com/scbk/sms/controller/HomeController.java`)
- `@Controller`
- `@GetMapping("/")`
- `public String index(Model model)`
  - `model.addAttribute("activeProfiles", ...)`
  - `model.addAttribute("menuSource", ...)`
  - `model.addAttribute("roleSource", ...)`
  - 반환값: `"index"`

#### LoginController (`src/main/java/com/scbk/sms/controller/LoginController.java`)
- `@Controller`
- `@GetMapping("/login")`
- `public String login(Model model)`
  - `model.addAttribute("localMode", ...)`
  - 반환값: `"login"`

### 3.2 sms 모듈
#### SmsHistoryController (`src/main/java/com/scbk/sms/controller/sms/SmsHistoryController.java`)
- `@Controller`
- `@RequestMapping("/sms/history")`
- `@GetMapping`
  - `public String page()`
  - 반환값: `"sms/history"`
- `@ResponseBody` + `@GetMapping("/data")`
  - `public ResponseEntity<ApiResponse<PageResponseDTO<SmsHistoryVO>>> getData(@ModelAttribute SmsHistorySearchRequestDTO request)`
  - 서비스 호출: `service.search(request)`
- `@ResponseBody` + `@PostMapping("/create")`
  - `public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody SmsHistoryUpdateRequestDTO request)`
  - 서비스 호출: `service.create(request)`
- `@ResponseBody` + `@PostMapping("/update")`
  - `public ResponseEntity<ApiResponse<String>> update(@Valid @RequestBody SmsHistoryUpdateRequestDTO request)`
  - 서비스 호출: `service.update(request)`
- `@ResponseBody` + `@PostMapping("/delete")`
  - `public ResponseEntity<ApiResponse<String>> delete(@RequestParam Integer smsHistoryId, @RequestParam String requestId)`
  - 서비스 호출: `service.delete(smsHistoryId, requestId)`

### 3.3 system/common-code 모듈
#### CommonCodeApiController (`src/main/java/com/scbk/sms/controller/system/CommonCodeApiController.java`)
- `@RestController`
- `@RequestMapping("/api/common-code")`
- `@GetMapping("/{codeType}")`
  - `public ResponseEntity<ApiResponse<List<CommonCodeVO>>> getCodes(@PathVariable String codeType, @RequestParam(required = false) String keyword)`
  - 서비스 호출: `commonCodeService.getCommonCodes(codeType, keyword)`
- 주석에 따르면 화면 콤보/자동완성용 API이며, 메뉴 권한 Interceptor 검증 대상에서 제외되지만 Spring Security 인증은 적용됨.

### 3.4 system/menu-tree 모듈
#### MenuTreeController (`src/main/java/com/scbk/sms/controller/system/MenuTreeController.java`)
- `@Controller`
- `@Profile("local")`
- `@RequestMapping("/system/menu-tree")`
- `@GetMapping`
  - `public String page(Model model)`
  - `model.addAttribute("activeProfiles", ...)`
  - `model.addAttribute("menuSource", ...)`
  - `model.addAttribute("roleSource", ...)`
  - 반환값: `"system/menu-tree"`
- 주석에 따르면 local 전용 메뉴 트리 확인 화면이며, 운영 메뉴 관리는 DB 메뉴 테이블 기준의 별도 구현 대상.

### 3.5 system/scaffold 모듈
#### ScaffoldController (`src/main/java/com/scbk/sms/controller/system/ScaffoldController.java`)
- `@Controller`
- `@Profile("local")`
- `@RequestMapping("/system/scaffold")`
- `@GetMapping`
  - `public String page()`
  - 반환값: `"system/scaffold"`
- `@ResponseBody` + `@PostMapping("/analyze")`
  - `public ResponseEntity<ApiResponse<Map<String, Object>>> analyze(@RequestBody Map<String, String> request)`
  - 서비스 호출: `scaffoldService.analyze(request.get("rawQuery"), request.get("targetTable"))`
- `@ResponseBody` + `@PostMapping("/generate")`
  - `public ResponseEntity<ApiResponse<Map<String, String>>> generate(@Valid @RequestBody ScaffoldRequestDTO request)`
  - 서비스 호출: `scaffoldService.generate(request)`
- `@ResponseBody` + `@PostMapping("/preview")`
  - `public ResponseEntity<ApiResponse<List<ScaffoldApplyFileResultDTO>>> preview(@Valid @RequestBody ScaffoldRequestDTO request)`
  - 서비스 호출: `scaffoldService.preview(request)`
- `@ResponseBody` + `@PostMapping("/apply")`
  - `public ResponseEntity<ApiResponse<List<ScaffoldApplyFileResultDTO>>> apply(@Valid @RequestBody ScaffoldRequestDTO request)`
  - 서비스 호출: `scaffoldService.apply(request)`
- 주석에 따르면 Query Scaffold 생성기이며 local 전용 개발 도구.

### 3.6 global model advice
#### GlobalModelAdvice (`src/main/java/com/scbk/sms/controller/GlobalModelAdvice.java`)
- `@ControllerAdvice`
- `@ModelAttribute`
- `public void addLayoutAttributes(@AuthenticationPrincipal SmsUserPrincipal principal, Model model, HttpServletRequest request)`
  - 인증 정보가 없는 경우 `pageAuth = PageAuth.none()`
  - 인증된 경우 `user`, `menus`, `pageAuth`를 모델에 추가
  - local profile에서는 `PageAuth.all()`
  - 그 외 profile에서는 요청 URI를 normalized path로 변환해 메뉴 권한 계산
- 엔드포인트는 아니지만 모든 화면에 공통 레이아웃/메뉴/페이지 권한 모델을 제공하는 적용 지점.

## 4. TODO/미완성 항목
> 검색 범위: `src/main/java`, `src/test/java`, `docs`, `db`, `docker`, `.claude/rules`, `AGENTS.md`, `CLAUDE.md`, `.gitignore`, `.vscode`. `src/main/resources/static/lib/*`는 외부 라이브러리이므로 제외.

| 위치 | 표시 | 확인 내용 |
|---|---|---|
| `src/main/java/com/scbk/sms/dto/sms/SmsHistoryUpdateRequestDTO.java:8` | TODO | 실제 수정을 허용할 필드만 남기고 제거 |
| `src/main/java/com/scbk/sms/service/system/scaffold/ScaffoldModel.java:291` | TODO | 상위 메뉴 ID |
| `src/main/java/com/scbk/sms/service/system/scaffold/ColumnTypeInferrer.java:69` | ORA-XXXXX | ORA-XXXXX 원문은 cause 체인 안쪽에 있으므로 루트 원인을 화면 메시지로 노출 |
| `src/main/java/com/scbk/sms/service/system/scaffold/ServiceTemplate.java:50` | TODO | 개인정보 컬럼을 MaskingUtil로 마스킹 |
| `src/main/java/com/scbk/sms/service/system/scaffold/ServiceTemplate.java:127` | TODO | column.maskType() 마스킹 정책에 맞게 MaskingUtil 적용 |
| `src/main/java/com/scbk/sms/service/system/scaffold/UpdateRequestDtoTemplate.java:31` | TODO | 실제 수정을 허용할 필드만 남기고 제거 |
| `src/main/java/com/scbk/sms/service/system/scaffold/UpdateRequestDtoTemplate.java:38` | TODO | PK 필드/WHERE 조건을 실제 PK 컬럼명으로 교체 |
| `src/main/java/com/scbk/sms/service/system/scaffold/ServiceTestTemplate.java:81` | TODO | 업무 규칙 테스트 추가 |
| `src/test/java/com/scbk/sms/service/sms/SmsHistoryServiceTest.java:71` | TODO | 업무 규칙 테스트 추가 |
| `src/test/java/com/scbk/sms/service/system/scaffold/ScaffoldTemplateTest.java:112` | TODO | 테스트가 생성 코드에서 `TODO: 테이블명`을 제거하는지 검증 |
| `src/test/java/com/scbk/sms/service/system/scaffold/ScaffoldTemplateTest.java:165` | TODO | 업무 규칙 테스트 추가 문구를 포함하는 테스트 |
| `docs/base/remaining-work.md:50` | TODO | `SmsHistoryMapper.xml`의 기존 CRUD TODO/bad SQL 제거 확인 |
| `docs/base/screen-generation-guide.md:95` | TODO | 테스트의 `// TODO` 부분에 업무 규칙 테스트를 채움 |
| `docs/base/test-automation-guide.md:22` | TODO | `// TODO: 업무 규칙 테스트` 부분을 사람/AI가 채움 |
| `docs/base/test-automation-guide.md:23` | TODO | 업무 규칙이 있는 화면은 TODO 채움 전까지 부분 완료 |
| `docs/base/test-automation-guide.md:68` | TODO | 화면 생성 → 테스트 TODO 채움 → `mvn test` → 실패 시 원인 수정 반복 |
| `docs/base/v2-scaffold-reference.md:60` | TODO | `ORDER BY A.ROWID DESC /* TODO */` 개선 검토 |
| `docs/base/common-response-contract.md:73` | XXX | 예측 가능한 업무 오류는 `CustomException(ErrorCode.XXX)`로 던짐 |
| `docs/base/query-scaffold-implementation.md:96` | TODO | 개인정보 Y 화면의 `/data`, `/excel`에 `@PrivacyLog` 자동 부착 + `MaskingUtil` 적용 지점 표시 |
| `docs/base/query-scaffold-implementation.md:147` | TODO | 개인정보 화면의 마스킹 TODO 보정 |
| `db/oracle/sms_history_menu_seed.sql:10` | TODO | 상위 메뉴 ID |
| `.claude/rules/testing.md:19` | TODO | scaffold가 생성한 ServiceTest/ControllerTest 배치 후 `// TODO` 업무 규칙 테스트 채움 |


## 5. 전체 요약
본 프로젝트는 Spring Boot 3.3.0 기반의 WAR 배포 구조이며, Java 21, Spring Web/Security/Validation/Actuator/AOP, Thymeleaf, MyBatis, Oracle JDBC를 중심으로 구성된다. 정적 화면은 Thymeleaf로 제공하고, 일부 API는 REST 컨트롤러로 노출하는 서버 사이드 렌더링 구조이다. Maven 의존성상 Apache POI, JSqlParser, Lombok, Spring Security LDAP, Thymeleaf Security 확장도 사용 중이다.

진입점은 `HomeController`, `LoginController`, `SmsHistoryController`, `CommonCodeApiController`, `MenuTreeController`, `ScaffoldController`으로 구성된다. 인증/로그인 영역은 홈과 로그인 페이지 진입만 담당하고, 실제 SMS/LMS 발신 이력 조회/생성/수정/삭제는 `/sms/history` 경로에서 처리한다. 공통코드와 스키폴더 생성기는 시스템 영역에 위치하며, 공통코드는 `GET /api/common-code/{codeType}`으로 코드 목록을 반환한다.

권한과 메뉴 관련 서비스/매퍼는 `com.scbk.sms.service.menu`와 `com.scbk.sms.mapper.menu` 패키지에 분산되어 있다. `MenuAuthMapper`, `EmployeeRoleMapper`, `MenuMapper`가 DB 접근을 담당하고, `MenuAuthService`, `EmployeeRoleService`, `DbMenuSource`, `StaticMenuSource`, `DbRoleProvider`, `StaticRoleProvider`가 DB/static 메뉴와 역할 제공 로직을 분담한다. `GlobalModelAdvice`는 컨트롤러별 진입점 목록에는 없지만 모든 화면에 사용자와 메뉴 트리, 페이지 권한을 공통으로 제공하는 핵심 적용 지점이다.

스캐폴드 영역은 local 전용 개발 도구로 운영된다. `ScaffoldController`는 `/system/scaffold` 경로에서 `analyze`, `generate`, `preview`, `apply` API를 제공하며, 내부 구현은 이 보고서에서 Service/Mapper 본문까지 읽지 않고 컨트롤러 시그니처만 확인했다. 문헌과 TODO를 종합하면 프로젝트의 방향은 실제 업무 화면을 직접 많이 만드는 것보다, Query Scaffold와 검증 루프를 통해 폐쇄망에서 반복 생성/검증 가능한 BASE 구조를 만드는 데 맞춰져 있다.

코드 품질 스캔에서는 TODO/FIXME/XXX가 확인되지 않은 것은 아니며, 대부분 scaffold 템플릿, SMS 이력 수정 DTO, 테스트 템플릿, 메뉴 시드, 문서의 업무 규칙 테스트 및 마스킹 관련 항목에 집중되어 있다. 외부 라이브러리인 `src/main/resources/static/lib/*`의 TODO는 제외했다. `@Repository` 어노테이션은 grep 결과에서 확인되지 않았다.
