package com.example.sms.service.system.scaffold;

/** Controller 테스트 생성. standalone MockMvc로 ApiResponse 포맷을 검증한다. */
public final class ControllerTestTemplate {

    private ControllerTestTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        String cls = model.domainClass();
        String module = model.moduleName();

        StringBuilder sb = new StringBuilder();
        sb.append("package com.example.sms.controller.").append(module).append(";\n\n")
          .append("import static org.mockito.ArgumentMatchers.any;\n")
          .append("import static org.mockito.BDDMockito.given;\n")
          .append("import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;\n")
          .append("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;\n")
          .append("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;\n\n")
          .append("import com.example.sms.dto.common.PageResponseDTO;\n")
          .append("import com.example.sms.dto.").append(module).append(".").append(cls).append("SearchRequestDTO;\n")
          .append("import com.example.sms.service.").append(module).append(".").append(cls).append("Service;\n")
          .append("import java.util.List;\n")
          .append("import org.junit.jupiter.api.BeforeEach;\n")
          .append("import org.junit.jupiter.api.Test;\n")
          .append("import org.junit.jupiter.api.extension.ExtendWith;\n")
          .append("import org.mockito.Mock;\n")
          .append("import org.mockito.junit.jupiter.MockitoExtension;\n")
          .append("import org.springframework.test.web.servlet.MockMvc;\n")
          .append("import org.springframework.test.web.servlet.setup.MockMvcBuilders;\n\n")
          .append("@ExtendWith(MockitoExtension.class)\n")
          .append("class ").append(cls).append("ControllerTest {\n\n")
          .append("    @Mock\n")
          .append("    private ").append(cls).append("Service service;\n\n")
          .append("    private MockMvc mockMvc;\n\n")
          .append("    @BeforeEach\n")
          .append("    void setUp() {\n")
          .append("        mockMvc = MockMvcBuilders.standaloneSetup(new ").append(cls).append("Controller(service)).build();\n")
          .append("    }\n\n")
          .append("    @Test\n")
          .append("    void data는_ApiResponse_포맷으로_응답한다() throws Exception {\n")
          .append("        // given\n")
          .append("        given(service.search(any())).willReturn(\n")
          .append("            PageResponseDTO.of(List.of(), new ").append(cls).append("SearchRequestDTO(), 0));\n\n")
          .append("        // when / then\n")
          .append("        mockMvc.perform(get(\"").append(model.screenUrl()).append("/data\"))\n")
          .append("            .andExpect(status().isOk())\n")
          .append("            .andExpect(jsonPath(\"$.code\").value(200))\n")
          .append("            .andExpect(jsonPath(\"$.data.totalCount\").value(0));\n")
          .append("    }\n")
          .append("}\n");
        return sb.toString();
    }
}
