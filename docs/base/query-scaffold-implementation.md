# Query Scaffold Implementation

QuerySpec(조회 SQL + `$변수`)에서 화면 1세트의 복사용 코드를 생성하는 local 전용 도구 구현 기록이다.

설계 근거는 `docs/base/v2-scaffold-reference.md`(v2 생성기 분석)를 따른다.

## 접근

- URL: `/system/scaffold` (생성 API: `POST /system/scaffold/generate`)
- `@Profile("local")` — dev/prod에서는 빈이 등록되지 않는다.
- 메뉴에 등록하지 않는 개발 도구다. `application-local.yml`의
  `sms.menu.auth.exclude-paths: /system/scaffold,/system/scaffold/**`로 local에서만 접근을 연다.

## 입력 (QuerySpec)

| 입력 | 예 |
|---|---|
| moduleName / domainId / domainClass / domainName | `sms` / `history` / `SmsHistory` / `발송이력조회` |
| rawQuery | `$변수` 검색조건 규약 포함 SQL |
| orderBy | `A.SEND_DT DESC, A.HIST_ID DESC` — 결정적 정렬 입력 필수 (v2에 없던 신규 입력) |
| includeCreateUpdate / includeExcel / includePrivacy | 선택 옵션 |

`$start_dt` 하나가 SearchRequestDTO 필드(`startDt`) + 화면 검색 input + XML `<if>` 동적조건 + `#{startDt}` 바인딩으로 동시 생성된다.

## 동작

```text
1. QueryColumnExtractor : JSQLParser로 SELECT 컬럼/alias 추출 (실패 시 정규식 fallback)
2. ColumnTypeInferrer   : SELECT * FROM (원쿼리) WHERE ROWNUM = 0 실행 후 메타데이터로 타입 추론
                          실패 시 String 강행하지 않고 오류 보고 (v2와 다른 v3 확정 동작)
3. ScaffoldService      : 11종 산출물 생성 -> 탭으로 복사용 코드 표시 (파일 직접 쓰기 없음)
```

산출물: SearchRequestDTO, VO, Mapper interface, Mapper XML, Service, Controller, ServiceTest, ControllerTest, HTML, JS, 메뉴등록 SQL — 기본 11종. 등록/수정 옵션 선택 시 UpdateRequestDTO(화이트리스트)가 추가되어 12종. (테스트 생성 전략은 `test-automation-guide.md`, 수정 요청 규약은 `screen-convention.md` "상세폼 화면 규약")

## 생성물의 v3 규약 (테스트로 고정)

- Lombok 기반: DTO/VO `@Data`, Service/Controller `@RequiredArgsConstructor` (2026-06-12 폐쇄망 Lombok 보유 확인으로 plain Java에서 전환)
- `ApiResponse<PageResponseDTO<VO>>` 응답 계약, `request.validate()` 호출
- `/create`, `/update` 분리. `/save` 생성 금지
- 메뉴 SQL은 v3 스키마 (`TB_MENU.MENU_ID/PARENT_MENU_ID`, `TB_MENU_AUTH.ROLE_CD` + `CAN_*` 8종)
- HTML은 `screen-convention.md` 카드 골격, JS는 `TuiPageBuilder`
- 개인정보 Y이면 `/data`, `/excel`에 `@PrivacyLog` 자동 부착 + `MaskingUtil` 적용 지점 TODO 표시
- 엑셀은 `ExcelUtil` 연결. Mapper의 Map 반환은 ExcelUtil 계약상 예외로 허용

## 생성 파일

| 파일 | 역할 |
|---|---|
| `controller/system/ScaffoldController.java` | 화면 + 생성 API |
| `service/system/ScaffoldService.java` | 생성 오케스트레이션 |
| `service/system/scaffold/QueryColumnExtractor.java` | 컬럼/검색변수 추출, 동적 SQL 변환 |
| `service/system/scaffold/ColumnTypeInferrer.java` | 실제 DB 기준 타입 추론 |
| `service/system/scaffold/*Template.java` (11종) | 산출물별 템플릿 (테스트 2종 포함) |
| `dto/system/ScaffoldRequestDTO.java` | QuerySpec 입력 |
| `templates/system/scaffold.html`, `static/js/system/scaffold.js` | 생성기 화면 |

v2는 컨트롤러 1파일 800줄이었으나, v3는 파일 300줄 규칙에 따라 분리해 핵심 로직을 단위 테스트로 고정했다.

## 의존성 (폐쇄망 반입 확인 완료)

| 의존성 | 버전 | 비고 |
|---|---|---|
| `com.github.jsqlparser:jsqlparser` | 4.9 | 회사 repo 보유 확인 (2026-06-11) |

## 검증 결과

2026-06-11 기준:

```text
mvn test                  PASS (41 tests, 신규 13: Extractor 5, Template 8)
mvn -DskipTests package   PASS
```

화면 검증(사용자, 서버 기동 후):

- local 로그인 후 `http://localhost:8081/system/scaffold` 접근
- 샘플 SQL + `$변수` 입력, 정렬 컬럼 입력 후 생성 -> 9개 탭 표시 확인
- 생성된 코드가 v3 규약(카드 골격, /create·/update, v3 메뉴 SQL)인지 확인

## 사용 절차

생성된 코드는 그대로 완료가 아니다. `screen-generation-guide.md`의 절차에 편입해서 사용한다.

1. 절차서 0~1단계(대상 확정, 실제 테이블 확인)를 먼저 수행한다.
2. scaffold로 2~7단계 산출물을 생성하고 검토 후 배치한다.
3. TODO 주석(테이블명, 마스킹, 상위 메뉴 ID)을 보정한다.
4. 절차서 8~10단계(권한, mvn 검증, 문서 갱신)를 수행한다.
