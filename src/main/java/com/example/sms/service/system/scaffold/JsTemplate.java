package com.example.sms.service.system.scaffold;

import java.util.List;

/** 화면 JS 생성. TuiPageBuilder로만 그리드를 초기화한다. */
public final class JsTemplate {

    private JsTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        List<String> vars = model.getSearchVars().isEmpty()
            ? List.of("searchKeyword")
            : model.getSearchVars();

        StringBuilder sb = new StringBuilder();
        sb.append("document.addEventListener('DOMContentLoaded', function () {\n")
          .append("    const pageBuilder = new TuiPageBuilder({\n")
          .append("        el: 'grid',\n")
          .append("        apiUrl: '").append(model.screenUrl()).append("/data',\n")
          .append("        searchInputs: [");
        for (int i = 0; i < vars.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("'").append(vars.get(i)).append("'");
        }
        sb.append("],\n")
          .append("        rowHeaders: ['rowNum'],\n")
          .append("        columns: [\n");

        List<String> columns = model.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i).trim();
            if (column.isEmpty()) {
                continue;
            }
            String javaType = model.getTypeMap().getOrDefault(column, "String");
            boolean isDateColumn = "LocalDate".equals(javaType) || "LocalDateTime".equals(javaType);

            sb.append("            { header: '").append(column)
              .append("', name: '").append(QueryColumnExtractor.toCamelCase(column))
              .append("', align: 'center', width: 150");
            if (isDateColumn) {
                sb.append(", formatter: TuiCommon.fmt.date");
            }
            sb.append(" }");
            if (i < columns.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("        ]\n")
          .append("    });\n");

        if (model.includeExcel()) {
            sb.append("\n    document.querySelector('#btn-excel')?.addEventListener('click', () => {\n")
              .append("        const qs = '?' + [");
            for (int i = 0; i < vars.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("`").append(vars.get(i)).append("=${document.querySelector('#").append(vars.get(i)).append("').value}`");
            }
            sb.append("].join('&');\n")
              .append("        window.location.href = '").append(model.screenUrl()).append("/excel' + qs;\n")
              .append("    });\n");
        }

        sb.append("});\n");
        return sb.toString();
    }
}
