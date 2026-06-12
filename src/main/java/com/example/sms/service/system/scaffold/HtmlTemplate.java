package com.example.sms.service.system.scaffold;

import java.util.List;

/** 화면 HTML 생성. screen-convention.md의 목록 화면 표준 골격을 따른다. */
public final class HtmlTemplate {

    private HtmlTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        List<String> vars = model.getSearchVars().isEmpty()
            ? List.of("searchKeyword")
            : model.getSearchVars();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n")
          .append("<html xmlns:th=\"http://www.thymeleaf.org\"\n")
          .append("      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n")
          .append("      layout:decorate=\"~{defaultLayout}\">\n")
          .append("<head>\n")
          .append("    <title>").append(model.domainName()).append("</title>\n")
          .append("</head>\n")
          .append("<body>\n")
          .append("<main layout:fragment=\"content\">\n\n")
          .append("    <div class=\"content-header\">\n")
          .append("        <h2>").append(model.domainName()).append("</h2>\n")
          .append("    </div>\n\n")
          .append("    <!-- 검색조건 카드 -->\n")
          .append("    <div class=\"card mb-4 shadow-sm border-0\">\n")
          .append("        <div class=\"card-body\">\n")
          .append("            <div class=\"row align-items-center g-3\">\n");

        for (String var : vars) {
            String inputType = isDateVar(var) ? "date" : "text";
            sb.append("                <div class=\"col-auto\"><label for=\"").append(var)
              .append("\" class=\"col-form-label fw-bold\">").append(var).append("</label></div>\n")
              .append("                <div class=\"col-auto\"><input type=\"").append(inputType)
              .append("\" id=\"").append(var).append("\" class=\"form-control\" style=\"width:160px;\"></div>\n");
        }

        sb.append("                <div class=\"col-auto ms-auto d-flex gap-2\">\n")
          .append("                    <button type=\"button\" id=\"btn-search\" class=\"btn btn-primary px-4\">조회</button>\n")
          .append("                    <button type=\"button\" id=\"btn-reset\" class=\"btn btn-secondary px-3\">초기화</button>\n");
        if (model.includeExcel()) {
            sb.append("                    <button type=\"button\" id=\"btn-excel\" class=\"btn btn-success ms-1\">엑셀</button>\n");
        }
        sb.append("                </div>\n")
          .append("            </div>\n")
          .append("        </div>\n")
          .append("    </div>\n\n")
          .append("    <!-- 그리드 카드 -->\n")
          .append("    <div class=\"card shadow-sm border-0 mb-4\">\n")
          .append("        <div class=\"card-header bg-white d-flex justify-content-between align-items-center py-3\">\n")
          .append("            <span class=\"fs-6 fw-medium\">총 <strong class=\"text-primary\" id=\"total-count\">0</strong>건</span>\n")
          .append("            <div class=\"d-flex align-items-center gap-2\">\n")
          .append("                <select id=\"pageSizeSelect\" class=\"form-select form-select-sm\" style=\"width:80px;\">\n")
          .append("                    <option value=\"10\">10건</option><option value=\"20\">20건</option><option value=\"50\">50건</option>\n")
          .append("                </select>\n")
          .append("            </div>\n")
          .append("        </div>\n")
          .append("        <div class=\"card-body p-0\"><div id=\"grid\" style=\"width:100%;\"></div></div>\n")
          .append("        <div class=\"card-footer bg-white border-0 py-3\"><div id=\"pagination\" class=\"d-flex justify-content-center\"></div></div>\n")
          .append("    </div>\n\n")
          .append("</main>\n")
          .append("<th:block layout:fragment=\"script\">\n")
          .append("    <script th:src=\"@{/js/").append(model.moduleName()).append("/").append(model.domainId()).append(".js}\"></script>\n")
          .append("</th:block>\n")
          .append("</body>\n")
          .append("</html>\n");
        return sb.toString();
    }

    private static boolean isDateVar(String var) {
        String lower = var.toLowerCase();
        return lower.contains("date") || lower.contains("dt") || lower.endsWith("at");
    }
}
