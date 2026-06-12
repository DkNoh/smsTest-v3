package com.example.sms.service.system.scaffold;

/**
 * 수정 요청 화이트리스트 DTO 생성.
 * VO를 update 요청 객체로 직접 사용하지 않는다 (mass assignment 방지).
 */
public final class UpdateRequestDtoTemplate {

    private UpdateRequestDtoTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        boolean hasLocalDate = model.getTypeMap().containsValue("LocalDate");
        boolean hasLocalDateTime = model.getTypeMap().containsValue("LocalDateTime");
        boolean hasBigDecimal = model.getTypeMap().containsValue("BigDecimal");

        StringBuilder sb = new StringBuilder();
        sb.append("package com.example.sms.dto.").append(model.moduleName()).append(";\n\n");
        if (hasBigDecimal) {
            sb.append("import java.math.BigDecimal;\n");
        }
        if (hasLocalDate) {
            sb.append("import java.time.LocalDate;\n");
        }
        if (hasLocalDateTime) {
            sb.append("import java.time.LocalDateTime;\n");
        }
        sb.append("import lombok.Data;\n\n")
          .append("/**\n")
          .append(" * 수정 가능한 필드만 선언하는 화이트리스트 DTO.\n")
          .append(" * TODO: 실제 수정을 허용할 필드만 남기고 제거한다.\n")
          .append(" *       REG_ID/REG_DTTM, 시스템 필드, 권한 필드는 선언하지 않는다.\n")
          .append(" */\n")
          .append("@Data\n")
          .append("public class ").append(model.domainClass()).append("UpdateRequestDTO {\n\n")
          .append("    // TODO: PK 필드 (WHERE 조건). 실제 PK 컬럼명으로 교체한다\n")
          .append("    private String id;\n\n");

        for (String column : model.getColumns()) {
            if (column.trim().isEmpty()) {
                continue;
            }
            String javaType = model.getTypeMap().getOrDefault(column, "String");
            sb.append("    private ").append(javaType).append(" ")
              .append(QueryColumnExtractor.toCamelCase(column.trim())).append(";\n");
        }

        sb.append("\n    /** 낙관적 잠금용. 조회 시점의 UPDATE_DTTM (hidden으로 받는다) */\n")
          .append("    private String beforeUpdateDttm;\n")
          .append("}\n");
        return sb.toString();
    }
}
