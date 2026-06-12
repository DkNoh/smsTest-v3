package com.example.sms.service.system.scaffold;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sms.dto.system.ScaffoldRequestDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScaffoldTemplateTest {

    private ScaffoldModel model(boolean createUpdate, boolean excel, boolean privacy) {
        ScaffoldRequestDTO request = new ScaffoldRequestDTO();
        request.setModuleName("sms");
        request.setDomainId("history");
        request.setDomainClass("SmsHistory");
        request.setDomainName("발송이력조회");
        request.setRawQuery("SELECT A.SEND_DT, A.RECEIVER_NO FROM SMS_HISTORY A WHERE 1=1\nAND A.SEND_DT >= $start_dt");
        request.setOrderBy("A.SEND_DT DESC, A.HIST_ID DESC");
        request.setIncludeCreateUpdate(createUpdate);
        request.setIncludeExcel(excel);
        request.setIncludePrivacy(privacy);
        return new ScaffoldModel(request,
            List.of("SEND_DT", "RECEIVER_NO"),
            List.of("startDt"),
            Map.of("SEND_DT", "LocalDate", "RECEIVER_NO", "String"));
    }

    @Test
    void DTO는_PageRequestDTO를_상속하고_Lombok_기반으로_생성한다() {
        // when
        String code = DtoTemplate.generate(model(false, false, false));

        // then
        assertThat(code).contains("extends PageRequestDTO");
        assertThat(code).contains("private String startDt;");
        assertThat(code).contains("@Data");
        assertThat(code).contains("@EqualsAndHashCode(callSuper = true)");
        assertThat(code).doesNotContain("public String getStartDt()");
    }

    @Test
    void VO는_추론된_타입과_Lombok으로_생성한다() {
        // when
        String code = VoTemplate.generate(model(false, false, false));

        // then
        assertThat(code).contains("@Data");
        assertThat(code).contains("private LocalDate sendDt;");
        assertThat(code).contains("private String receiverNo;");
        assertThat(code).contains("import java.time.LocalDate;");
        assertThat(code).doesNotContain("public LocalDate getSendDt()");
    }

    @Test
    void Service와_Controller는_RequiredArgsConstructor를_사용한다() {
        // when
        String serviceCode = ServiceTemplate.generate(model(false, false, false));
        String controllerCode = ControllerTemplate.generate(model(false, false, false));

        // then
        assertThat(serviceCode).contains("@RequiredArgsConstructor");
        assertThat(serviceCode).doesNotContain("public SmsHistoryService(");
        assertThat(controllerCode).contains("@RequiredArgsConstructor");
        assertThat(controllerCode).doesNotContain("public SmsHistoryController(");
    }

    @Test
    void Controller는_create와_update를_분리하고_save를_만들지_않는다() {
        // when
        String code = ControllerTemplate.generate(model(true, false, false));

        // then
        assertThat(code).contains("@PostMapping(\"/create\")");
        assertThat(code).contains("@PostMapping(\"/update\")");
        assertThat(code).doesNotContain("/save");
    }

    @Test
    void 수정_요청은_화이트리스트_DTO로만_받는다() {
        // when
        String controllerCode = ControllerTemplate.generate(model(true, false, false));
        String updateDtoCode = UpdateRequestDtoTemplate.generate(model(true, false, false));
        String serviceCode = ServiceTemplate.generate(model(true, false, false));
        String xmlCode = MapperXmlTemplate.generate(model(true, false, false));

        // then : Controller는 VO가 아니라 UpdateRequestDTO를 수신한다
        assertThat(controllerCode).contains("@RequestBody SmsHistoryUpdateRequestDTO request");
        assertThat(controllerCode).doesNotContain("@RequestBody SmsHistoryVO");

        // UpdateRequestDTO는 화이트리스트 안내와 낙관적 잠금 필드를 가진다
        assertThat(updateDtoCode).contains("수정을 허용할 필드만 남기고");
        assertThat(updateDtoCode).contains("private String beforeUpdateDttm;");

        // Service는 update 0건을 충돌로 실패 처리한다
        assertThat(serviceCode).contains("ErrorCode.UPDATE_CONFLICT");

        // XML은 낙관적 잠금 조건을 포함한다
        assertThat(xmlCode).contains("AND UPD_DTTM = #{beforeUpdateDttm}");
    }

    @Test
    void 개인정보_포함이면_PrivacyLog를_부착한다() {
        // when
        String code = ControllerTemplate.generate(model(false, true, true));

        // then
        assertThat(code).contains("@PrivacyLog(action = \"발송이력조회 목록 조회\")");
        assertThat(code).contains("@PrivacyLog(action = \"발송이력조회 엑셀 다운로드\")");
    }

    @Test
    void MapperXml은_입력받은_정렬과_OFFSET_FETCH를_사용한다() {
        // when
        String xml = MapperXmlTemplate.generate(model(false, false, false));

        // then
        assertThat(xml).contains("ORDER BY A.SEND_DT DESC, A.HIST_ID DESC");
        assertThat(xml).contains("OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY");
        assertThat(xml).contains("<if test=\"startDt != null and startDt != ''\">");
    }

    @Test
    void 메뉴SQL은_v3_스키마를_사용한다() {
        // when
        String sql = MenuSqlTemplate.generate(model(false, false, false));

        // then
        assertThat(sql).contains("SMS.TB_MENU (");
        assertThat(sql).contains("MENU_ID, PARENT_MENU_ID");
        assertThat(sql).contains("CAN_READ, CAN_CREATE, CAN_UPDATE, CAN_DELETE");
        assertThat(sql).contains("CAN_APPROVE, CAN_CANCEL, CAN_DOWNLOAD, CAN_MASK_VIEW");
        assertThat(sql).contains("ROLE_CD");
        // v2 스키마 잔재가 없어야 한다
        assertThat(sql).doesNotContain("AUTH_CD");
        assertThat(sql).doesNotContain("CAN_WRITE");
        assertThat(sql).doesNotContain("UP_MENU_CD");
    }

    @Test
    void ServiceTest는_Mockito_mock과_given_when_then으로_생성한다() {
        // when
        String code = ServiceTestTemplate.generate(model(true, false, false));

        // then
        assertThat(code).contains("@ExtendWith(MockitoExtension.class)");
        assertThat(code).contains("@Mock");
        assertThat(code).contains("// given");
        assertThat(code).contains("// when");
        assertThat(code).contains("// then");
        assertThat(code).contains("then(mapper).should().delete(\"1\")");
        assertThat(code).contains("// TODO: 업무 규칙 테스트를 추가한다");
    }

    @Test
    void ControllerTest는_MockMvc로_ApiResponse_포맷을_검증한다() {
        // when
        String code = ControllerTestTemplate.generate(model(false, false, false));

        // then
        assertThat(code).contains("MockMvcBuilders.standaloneSetup");
        assertThat(code).contains("get(\"/sms/history/data\")");
        assertThat(code).contains("jsonPath(\"$.code\").value(200)");
    }

    @Test
    void HTML은_screen_convention_골격을_따른다() {
        // when
        String html = HtmlTemplate.generate(model(false, true, false));

        // then
        assertThat(html).contains("layout:decorate=\"~{defaultLayout}\"");
        assertThat(html).contains("id=\"btn-search\"");
        assertThat(html).contains("id=\"total-count\"");
        assertThat(html).contains("id=\"pageSizeSelect\"");
        assertThat(html).contains("id=\"btn-excel\"");
        assertThat(html).doesNotContain("search-section");
    }

    @Test
    void JS는_TuiPageBuilder로_그리드를_초기화한다() {
        // when
        String js = JsTemplate.generate(model(false, false, false));

        // then
        assertThat(js).contains("new TuiPageBuilder({");
        assertThat(js).contains("apiUrl: '/sms/history/data'");
        assertThat(js).contains("searchInputs: ['startDt']");
        assertThat(js).contains("name: 'sendDt'");
    }

    @Test
    void JS는_날짜_타입_컬럼에_공통_포매터를_부착한다() {
        // when : SEND_DT는 LocalDate, RECEIVER_NO는 String
        String js = JsTemplate.generate(model(false, false, false));

        // then
        assertThat(js).contains("name: 'sendDt', align: 'center', width: 150, formatter: TuiCommon.fmt.date");
        assertThat(js).contains("name: 'receiverNo', align: 'center', width: 150 }");
    }

    @Test
    void MapperXml은_날짜_비교_가이드_주석을_포함한다() {
        // when
        String xml = MapperXmlTemplate.generate(model(false, false, false));

        // then
        assertThat(xml).contains("YYYYMMDD");
        assertThat(xml).contains("TO_DATE");
    }
}
