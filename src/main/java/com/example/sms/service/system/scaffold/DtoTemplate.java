package com.example.sms.service.system.scaffold;

import java.util.List;

/** SearchRequestDTO 생성. PageRequestDTO 상속, Lombok 기반. */
public final class DtoTemplate {

    private DtoTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        List<String> vars = model.getSearchVars().isEmpty()
            ? List.of("searchKeyword")
            : model.getSearchVars();

        StringBuilder sb = new StringBuilder();
        sb.append("package com.example.sms.dto.").append(model.moduleName()).append(";\n\n")
          .append("import com.example.sms.dto.common.PageRequestDTO;\n")
          .append("import lombok.Data;\n")
          .append("import lombok.EqualsAndHashCode;\n\n")
          .append("@Data\n")
          .append("@EqualsAndHashCode(callSuper = true)\n")
          .append("public class ").append(model.domainClass()).append("SearchRequestDTO extends PageRequestDTO {\n\n");

        for (String var : vars) {
            sb.append("    private String ").append(var).append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
