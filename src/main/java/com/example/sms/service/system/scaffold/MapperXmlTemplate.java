package com.example.sms.service.system.scaffold;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Mapper XML 생성. $변수 라인은 동적 <if> 조건으로 변환된다. */
public final class MapperXmlTemplate {

    private static final Pattern SEARCH_VAR_PATTERN = Pattern.compile("\\$([a-zA-Z0-9_]+)");
    private static final Pattern COLUMN_COMPARE_PATTERN =
        Pattern.compile("([A-Za-z0-9_\\.]+)\\s*(<>|>=|<=|=|>|<)\\s*\\$([a-zA-Z0-9_]+)");

    private MapperXmlTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        String cls = model.domainClass();
        String module = model.moduleName();
        String dynamicQuery = convertToDynamicSql(model, "            ");
        String targetTable = model.targetTable();
        if (model.includeCreateUpdate() && targetTable.isEmpty()) {
            throw new IllegalStateException("CRUD 모드는 targetTable이 필요합니다. 조회 SQL의 FROM 테이블을 추론할 수 없으면 수정 대상 테이블을 입력하세요.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n")
          .append("<!DOCTYPE mapper\n")
          .append("        PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n")
          .append("        \"https://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n")
          .append("<mapper namespace=\"com.example.sms.mapper.").append(module).append(".").append(cls).append("Mapper\">\n\n")
          .append("    <!-- 화면 DatePicker 검색값은 YYYYMMDD 문자열로 전달된다 (TuiPageBuilder 규약).\n")
          .append("         비교 컬럼이 DATE/TIMESTAMP면 TO_DATE/TO_TIMESTAMP로 감싸고,\n")
          .append("         단일 날짜 = 조건은 당일 00:00:00 이상, 다음날 00:00:00 미만 범위로 변환한다. -->\n\n");

        sb.append("    <sql id=\"searchConditions\">\n")
          .append("        <where>\n");
        if (model.getSearchVars().isEmpty() && !model.getColumns().isEmpty()) {
            String firstColumn = model.getColumns().get(0).trim().toUpperCase();
            sb.append("            <if test=\"searchKeyword != null and searchKeyword != ''\">\n")
              .append("                AND A.").append(firstColumn).append(" LIKE '%' || #{searchKeyword} || '%'\n")
              .append("            </if>\n");
        }
        sb.append("        </where>\n")
          .append("    </sql>\n\n");

        sb.append("    <select id=\"count\" resultType=\"int\">\n")
          .append("        SELECT COUNT(1) FROM (\n")
          .append(dynamicQuery)
          .append("        ) A\n")
          .append("        <include refid=\"searchConditions\"/>\n")
          .append("    </select>\n\n");

        sb.append("    <select id=\"selectList\" resultType=\"com.example.sms.vo.")
          .append(module).append(".").append(cls).append("VO\">\n")
          .append("        SELECT A.*\n")
          .append("        FROM (\n")
          .append(dynamicQuery)
          .append("        ) A\n")
          .append("        <include refid=\"searchConditions\"/>\n")
          .append("        ORDER BY ").append(model.orderBy()).append("\n")
          .append("        OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY\n")
          .append("    </select>\n");

        if (model.includeCreateUpdate()) {
            sb.append("\n    <insert id=\"insert\">\n")
              .append(insertSql(model, targetTable))
              .append("    </insert>\n\n")
              .append("    <!-- update 기준: 수정 허용 컬럼만 SET하고, 잠금 컬럼을 지정한 경우 WHERE에 함께 둔다. -->\n")
              .append("    <update id=\"update\">\n")
              .append("        UPDATE ").append(targetTable).append("\n")
              .append(updateSetClause(model))
              .append("         WHERE ").append(pkWhereColumn(model)).append(" = #{").append(model.pkFieldName()).append("}\n")
              .append(lockWhereClause(model))
              .append("    </update>\n\n")
              .append("    <delete id=\"delete\">\n")
              .append("        DELETE FROM ").append(targetTable).append(" WHERE ").append(pkWhereColumn(model))
              .append(" = #{").append(model.pkFieldName()).append("}\n")
              .append("    </delete>\n");
        }

        if (model.includeExcel()) {
            sb.append("\n    <select id=\"selectListForExcel\" resultType=\"java.util.HashMap\">\n")
              .append("        SELECT A.*\n")
              .append("        FROM (\n")
              .append(dynamicQuery)
              .append("        ) A\n")
              .append("        <include refid=\"searchConditions\"/>\n")
              .append("        ORDER BY ").append(model.orderBy()).append("\n")
              .append("    </select>\n");
        }

        sb.append("\n</mapper>\n");
        return sb.toString();
    }

    private static String convertToDynamicSql(ScaffoldModel model, String indent) {
        Map<String, ScaffoldModel.SearchParam> paramMap = new HashMap<>();
        for (ScaffoldModel.SearchParam param : model.searchParams()) {
            paramMap.put(param.name(), param);
        }

        StringBuilder sql = new StringBuilder();
        for (String line : model.rawQuery().split("\n")) {
            if (!line.contains("$")) {
                sql.append(indent).append(escapeSqlText(line)).append("\n");
                continue;
            }

            java.util.List<String> lineVars = new java.util.ArrayList<>();
            Matcher matcher = SEARCH_VAR_PATTERN.matcher(line);
            while (matcher.find()) {
                lineVars.add(QueryColumnExtractor.toCamelCase(matcher.group(1)));
            }

            sql.append(indent).append("    <if test=\"");
            for (int i = 0; i < lineVars.size(); i++) {
                if (i > 0) {
                    sql.append(" and ");
                }
                sql.append(lineVars.get(i)).append(" != null and ").append(lineVars.get(i)).append(" != ''");
            }
            sql.append("\">\n");

            String replacedLine = replaceBindVariables(model, paramMap, line);
            sql.append(indent).append("        ").append(replacedLine.trim()).append("\n");
            sql.append(indent).append("    </if>\n");
        }
        return sql.toString();
    }

    private static String replaceBindVariables(ScaffoldModel model,
                                               Map<String, ScaffoldModel.SearchParam> paramMap,
                                               String line) {
        Matcher compareMatcher = COLUMN_COMPARE_PATTERN.matcher(line);
        StringBuffer buffer = new StringBuffer();
        while (compareMatcher.find()) {
            String columnRef = compareMatcher.group(1);
            String operator = compareMatcher.group(2);
            String rawVar = compareMatcher.group(3);
            String fieldName = QueryColumnExtractor.toCamelCase(rawVar);
            String replacement = compareReplacement(model, paramMap.get(fieldName), columnRef, operator, fieldName);
            compareMatcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        compareMatcher.appendTail(buffer);

        return SEARCH_VAR_PATTERN.matcher(buffer.toString())
            .replaceAll(match -> "#{" + QueryColumnExtractor.toCamelCase(match.group(1)) + "}");
    }

    private static String compareReplacement(ScaffoldModel model,
                                             ScaffoldModel.SearchParam param,
                                             String columnRef,
                                             String operator,
                                             String fieldName) {
        if (param == null || !param.isDate() || !"=".equals(operator)) {
            return columnRef + " " + xmlOperator(operator) + " "
                + bindExpression(model, param, columnRef, operator, fieldName);
        }

        String javaType = model.getTypeMap().getOrDefault(columnName(columnRef).toUpperCase(), "");
        if ("LocalDateTime".equals(javaType)) {
            String start = "TO_TIMESTAMP(#{" + fieldName + "} || '000000', 'YYYYMMDDHH24MISS')";
            return columnRef + " " + xmlOperator(">=") + " " + start
                + " AND " + columnRef + " " + xmlOperator("<") + " " + start + " + INTERVAL '1' DAY";
        }
        if ("LocalDate".equals(javaType)) {
            String start = "TO_DATE(#{" + fieldName + "}, 'YYYYMMDD')";
            return columnRef + " " + xmlOperator(">=") + " " + start
                + " AND " + columnRef + " " + xmlOperator("<") + " " + start + " + 1";
        }
        return columnRef + " " + xmlOperator(operator) + " #{" + fieldName + "}";
    }

    private static String bindExpression(ScaffoldModel model,
                                         ScaffoldModel.SearchParam param,
                                         String columnRef,
                                         String operator,
                                         String fieldName) {
        if (param == null || !param.isDate()) {
            return "#{" + fieldName + "}";
        }
        String columnName = columnName(columnRef);
        String javaType = model.getTypeMap().getOrDefault(columnName.toUpperCase(), "");
        if ("LocalDateTime".equals(javaType)) {
            String suffix = isUpperBound(operator, fieldName) ? "235959" : "000000";
            return "TO_TIMESTAMP(#{" + fieldName + "} || '" + suffix + "', 'YYYYMMDDHH24MISS')";
        }
        if ("LocalDate".equals(javaType)) {
            return "TO_DATE(#{" + fieldName + "}, 'YYYYMMDD')";
        }
        return "#{" + fieldName + "}";
    }

    private static String columnName(String columnRef) {
        return columnRef.contains(".")
            ? columnRef.substring(columnRef.lastIndexOf('.') + 1)
            : columnRef;
    }

    private static String escapeSqlText(String sqlLine) {
        return sqlLine.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private static String xmlOperator(String operator) {
        if (operator.contains("<") || operator.contains(">")) {
            return "<![CDATA[ " + operator + " ]]>";
        }
        return operator;
    }

    private static boolean isUpperBound(String operator, String fieldName) {
        return "<=".equals(operator) || "<".equals(operator)
            || fieldName.endsWith("To") || fieldName.startsWith("end") || fieldName.startsWith("to");
    }

    private static String pkWhereColumn(ScaffoldModel model) {
        return model.pkColumn().isEmpty() ? "ID" : model.pkColumn();
    }

    private static String insertSql(ScaffoldModel model, String targetTable) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (ScaffoldModel.ColumnConfig column : editableColumns(model)) {
            columns.add(column.columnName());
            values.add("#{" + column.fieldName() + "}");
        }

        if (hasColumn(model, "REG_DTTM")) {
            columns.add("REG_DTTM");
            values.add(currentValueExpression(model, "REG_DTTM"));
        }
        if (!model.lockColumn().isEmpty()) {
            columns.add(model.lockColumn());
            values.add(currentValueExpression(model, model.lockColumn()));
        } else if (hasColumn(model, "UPD_DTTM")) {
            columns.add("UPD_DTTM");
            values.add(currentValueExpression(model, "UPD_DTTM"));
        }

        if (columns.isEmpty()) {
            throw new IllegalStateException("CRUD INSERT를 생성할 수정 가능 컬럼이 없습니다. 컬럼 옵션에서 등록/수정할 컬럼을 선택하세요.");
        }

        return "        INSERT INTO " + targetTable + " (\n"
            + "            " + String.join(",\n            ", columns) + "\n"
            + "        ) VALUES (\n"
            + "            " + String.join(",\n            ", values) + "\n"
            + "        )\n";
    }

    private static String updateSetClause(ScaffoldModel model) {
        List<String> assignments = new ArrayList<>();
        for (ScaffoldModel.ColumnConfig column : editableColumns(model)) {
            assignments.add(column.columnName() + " = #{" + column.fieldName() + "}");
        }
        if (!model.lockColumn().isEmpty()) {
            assignments.add(model.lockColumn() + " = " + currentValueExpression(model, model.lockColumn()));
        }
        if (assignments.isEmpty()) {
            assignments.add(pkWhereColumn(model) + " = " + pkWhereColumn(model));
        }

        StringBuilder sb = new StringBuilder("           SET ");
        for (int i = 0; i < assignments.size(); i++) {
            if (i > 0) {
                sb.append("               ");
            }
            sb.append(assignments.get(i));
            if (i < assignments.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static List<ScaffoldModel.ColumnConfig> editableColumns(ScaffoldModel model) {
        return model.columnConfigs().stream()
            .filter(ScaffoldModel.ColumnConfig::editable)
            .toList();
    }

    private static boolean hasColumn(ScaffoldModel model, String columnName) {
        return model.getColumns().stream()
            .map(String::trim)
            .map(String::toUpperCase)
            .anyMatch(columnName::equals);
    }

    private static String currentValueExpression(ScaffoldModel model, String columnName) {
        String normalized = columnName.toUpperCase();
        String javaType = model.getTypeMap().getOrDefault(normalized, "");
        if ("LocalDate".equals(javaType)) {
            return "TRUNC(SYSDATE)";
        }
        if ("String".equals(javaType) && (normalized.endsWith("DTTM") || normalized.endsWith("_DTM"))) {
            return "TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS')";
        }
        return "SYSTIMESTAMP";
    }

    private static String lockWhereClause(ScaffoldModel model) {
        if (model.lockColumn().isEmpty()) {
            return "";
        }
        return "           AND " + model.lockColumn() + " = #{" + model.beforeLockFieldName() + "}\n";
    }
}
