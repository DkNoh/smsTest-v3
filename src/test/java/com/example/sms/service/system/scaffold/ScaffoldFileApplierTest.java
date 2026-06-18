package com.example.sms.service.system.scaffold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.sms.dto.system.ScaffoldApplyFileResultDTO;
import com.example.sms.dto.system.ScaffoldRequestDTO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScaffoldFileApplierTest {

    @TempDir
    private Path tempDir;

    @Test
    void 산출물을_정해진_프로젝트_폴더로_저장한다() throws Exception {
        // given
        ScaffoldRequestDTO request = request("sms", "history", "SmsHistory");
        Map<String, String> generatedFiles = new LinkedHashMap<>();
        generatedFiles.put("SmsHistorySearchRequestDTO.java", "search dto");
        generatedFiles.put("SmsHistoryUpdateRequestDTO.java", "update dto");
        generatedFiles.put("SmsHistoryVO.java", "vo");
        generatedFiles.put("SmsHistoryMapper.java", "mapper");
        generatedFiles.put("SmsHistoryMapper.xml", "mapper xml");
        generatedFiles.put("SmsHistoryService.java", "service");
        generatedFiles.put("SmsHistoryController.java", "controller");
        generatedFiles.put("SmsHistoryServiceTest.java", "service test");
        generatedFiles.put("SmsHistoryControllerTest.java", "controller test");
        generatedFiles.put("history.html", "html");
        generatedFiles.put("history.js", "js");
        generatedFiles.put("메뉴등록.sql", "menu sql");

        ScaffoldFileApplier applier = new ScaffoldFileApplier(tempDir);

        // when
        List<ScaffoldApplyFileResultDTO> appliedFiles = applier.apply(request, generatedFiles);
        Map<String, String> appliedPaths = pathMap(appliedFiles);

        // then
        assertThat(appliedPaths)
            .containsEntry("SmsHistorySearchRequestDTO.java",
                "src/main/java/com/example/sms/dto/sms/SmsHistorySearchRequestDTO.java")
            .containsEntry("SmsHistoryUpdateRequestDTO.java",
                "src/main/java/com/example/sms/dto/sms/SmsHistoryUpdateRequestDTO.java")
            .containsEntry("SmsHistoryVO.java",
                "src/main/java/com/example/sms/vo/sms/SmsHistoryVO.java")
            .containsEntry("SmsHistoryMapper.java",
                "src/main/java/com/example/sms/mapper/sms/SmsHistoryMapper.java")
            .containsEntry("SmsHistoryMapper.xml",
                "src/main/resources/mapper/sms/SmsHistoryMapper.xml")
            .containsEntry("SmsHistoryService.java",
                "src/main/java/com/example/sms/service/sms/SmsHistoryService.java")
            .containsEntry("SmsHistoryController.java",
                "src/main/java/com/example/sms/controller/sms/SmsHistoryController.java")
            .containsEntry("SmsHistoryServiceTest.java",
                "src/test/java/com/example/sms/service/sms/SmsHistoryServiceTest.java")
            .containsEntry("SmsHistoryControllerTest.java",
                "src/test/java/com/example/sms/controller/sms/SmsHistoryControllerTest.java")
            .containsEntry("history.html",
                "src/main/resources/templates/sms/history.html")
            .containsEntry("history.js",
                "src/main/resources/static/js/sms/history.js")
            .containsEntry("메뉴등록.sql",
                "db/oracle/sms_history_menu_seed.sql");

        assertThat(Files.readString(tempDir.resolve(
            "src/main/java/com/example/sms/dto/sms/SmsHistorySearchRequestDTO.java"))).isEqualTo("search dto");
        assertThat(Files.readString(tempDir.resolve(
            "src/main/resources/static/js/sms/history.js"))).isEqualTo("js");
        assertThat(Files.readString(tempDir.resolve(
            "db/oracle/sms_history_menu_seed.sql"))).isEqualTo("menu sql");
    }

    @Test
    void 미리보기는_신규_변경없음_덮어쓰기를_구분한다() throws Exception {
        // given
        ScaffoldRequestDTO request = request("sms", "history", "SmsHistory");
        Path sameFile = tempDir.resolve("src/main/java/com/example/sms/vo/sms/SmsHistoryVO.java");
        Path changedFile = tempDir.resolve("src/main/resources/static/js/sms/history.js");
        Files.createDirectories(sameFile.getParent());
        Files.createDirectories(changedFile.getParent());
        Files.writeString(sameFile, "same");
        Files.writeString(changedFile, "old");

        Map<String, String> generatedFiles = new LinkedHashMap<>();
        generatedFiles.put("SmsHistoryVO.java", "same");
        generatedFiles.put("history.js", "new");
        generatedFiles.put("메뉴등록.sql", "menu sql");

        ScaffoldFileApplier applier = new ScaffoldFileApplier(tempDir);

        // when
        List<ScaffoldApplyFileResultDTO> preview = applier.preview(request, generatedFiles);

        // then
        assertThat(statusMap(preview))
            .containsEntry("SmsHistoryVO.java", "UNCHANGED")
            .containsEntry("history.js", "OVERWRITE")
            .containsEntry("메뉴등록.sql", "NEW");
    }

    @Test
    void 경로로_쓸_수_없는_입력은_거부한다() {
        // given
        ScaffoldRequestDTO request = request("../sms", "history", "SmsHistory");
        ScaffoldFileApplier applier = new ScaffoldFileApplier(tempDir);

        // when / then
        assertThatThrownBy(() -> applier.apply(request, Map.of("SmsHistoryVO.java", "vo")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("moduleName");
    }

    private ScaffoldRequestDTO request(String moduleName, String domainId, String domainClass) {
        ScaffoldRequestDTO request = new ScaffoldRequestDTO();
        request.setModuleName(moduleName);
        request.setDomainId(domainId);
        request.setDomainClass(domainClass);
        request.setDomainName("발송이력조회");
        request.setRawQuery("SELECT A.SEND_DT FROM SMS_HISTORY A WHERE 1=1");
        request.setOrderBy("A.SEND_DT DESC");
        return request;
    }

    private Map<String, String> pathMap(List<ScaffoldApplyFileResultDTO> appliedFiles) {
        Map<String, String> result = new LinkedHashMap<>();
        for (ScaffoldApplyFileResultDTO appliedFile : appliedFiles) {
            result.put(appliedFile.getFileName(), appliedFile.getPath());
        }
        return result;
    }

    private Map<String, String> statusMap(List<ScaffoldApplyFileResultDTO> appliedFiles) {
        Map<String, String> result = new LinkedHashMap<>();
        for (ScaffoldApplyFileResultDTO appliedFile : appliedFiles) {
            result.put(appliedFile.getFileName(), appliedFile.getStatus());
        }
        return result;
    }
}
