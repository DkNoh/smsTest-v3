package com.example.sms.service.system.scaffold;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * rawQuery에서 SELECT 컬럼과 $검색변수를 추출한다.
 * JSQLParser 파싱을 우선하고, 실패하면 정규식으로 추출한다.
 */
public final class QueryColumnExtractor {

    private static final Pattern SEARCH_VAR_PATTERN = Pattern.compile("\\$([a-zA-Z0-9_]+)");
    private static final Pattern SELECT_PART_PATTERN =
        Pattern.compile("SELECT(.*?)\\s+FROM\\s", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private QueryColumnExtractor() {
    }

    /** SELECT 컬럼명(alias 우선)을 추출한다. */
    public static List<String> extractColumns(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String safeQuery = query
            .replaceAll("#\\{[^}]+\\}", "NULL")
            .replaceAll("\\$\\{[^}]+\\}", "NULL")
            .replaceAll("\\$([a-zA-Z0-9_]+)", "NULL");

        try {
            net.sf.jsqlparser.statement.Statement stmt = CCJSqlParserUtil.parse(safeQuery);
            if (!(stmt instanceof Select select) || select.getPlainSelect() == null) {
                throw new IllegalStateException("PlainSelect 구조가 아닙니다.");
            }
            PlainSelect plainSelect = select.getPlainSelect();

            List<String> columns = new ArrayList<>();
            for (SelectItem<?> item : plainSelect.getSelectItems()) {
                String alias = item.getAlias() != null ? item.getAlias().getName() : null;
                if (alias != null) {
                    columns.add(alias.replaceAll("^\"|\"$", ""));
                } else if (item.getExpression() instanceof Column column) {
                    columns.add(column.getColumnName());
                } else {
                    String expr = item.getExpression().toString();
                    String last = lastIdentifier(expr);
                    if (!last.isEmpty()) {
                        columns.add(last);
                    }
                }
            }
            return columns;
        } catch (Exception e) {
            return extractColumnsFallback(query);
        }
    }

    /** $변수를 camelCase로 추출한다. $base_dt -> baseDt */
    public static List<String> extractSearchVars(String query) {
        List<String> vars = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return vars;
        }
        Matcher matcher = SEARCH_VAR_PATTERN.matcher(query);
        while (matcher.find()) {
            String varName = toCamelCase(matcher.group(1));
            if (!vars.contains(varName)) {
                vars.add(varName);
            }
        }
        return vars;
    }

    /**
     * $변수가 포함된 라인을 MyBatis 동적 조건으로 변환한다.
     * AND A.SEND_DT >= $start_dt
     *   -> <if test="startDt != null and startDt != ''"> AND A.SEND_DT >= #{startDt} </if>
     */
    public static String convertToDynamicSql(String rawQuery, String indent) {
        StringBuilder sql = new StringBuilder();
        for (String line : rawQuery.split("\n")) {
            if (!line.contains("$")) {
                sql.append(indent).append(line).append("\n");
                continue;
            }

            List<String> lineVars = new ArrayList<>();
            Matcher matcher = SEARCH_VAR_PATTERN.matcher(line);
            while (matcher.find()) {
                lineVars.add(toCamelCase(matcher.group(1)));
            }

            sql.append(indent).append("    <if test=\"");
            for (int i = 0; i < lineVars.size(); i++) {
                if (i > 0) {
                    sql.append(" and ");
                }
                sql.append(lineVars.get(i)).append(" != null and ").append(lineVars.get(i)).append(" != ''");
            }
            sql.append("\">\n");

            String replacedLine = SEARCH_VAR_PATTERN.matcher(line)
                .replaceAll(match -> "#{" + toCamelCase(match.group(1)) + "}");
            sql.append(indent).append("        ").append(replacedLine.trim()).append("\n");
            sql.append(indent).append("    </if>\n");
        }
        return sql.toString();
    }

    /** snake_case -> camelCase. map-underscore-to-camel-case 설정과 일치시킨다. */
    public static String toCamelCase(String value) {
        String[] parts = value.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    private static List<String> extractColumnsFallback(String query) {
        List<String> columns = new ArrayList<>();
        Matcher matcher = SELECT_PART_PATTERN.matcher(query);
        if (!matcher.find()) {
            return columns;
        }
        for (String part : matcher.group(1).split(",")) {
            part = part.trim();
            if (part.isEmpty()) {
                continue;
            }
            String[] tokens = part.split("\\s+");
            String colName = tokens[tokens.length - 1];
            if (colName.contains(".")) {
                colName = colName.substring(colName.lastIndexOf(".") + 1);
            }
            columns.add(colName);
        }
        return columns;
    }

    private static String lastIdentifier(String expression) {
        String last = "";
        for (String token : expression.split("[^a-zA-Z0-9_]")) {
            if (!token.trim().isEmpty()) {
                last = token.trim();
            }
        }
        return last;
    }
}
