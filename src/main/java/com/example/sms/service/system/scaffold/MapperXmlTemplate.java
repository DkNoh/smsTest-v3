package com.example.sms.service.system.scaffold;

/** Mapper XML 생성. $변수 라인은 동적 <if> 조건으로 변환된다. */
public final class MapperXmlTemplate {

    private MapperXmlTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        String cls = model.domainClass();
        String module = model.moduleName();
        String dynamicQuery = QueryColumnExtractor.convertToDynamicSql(model.rawQuery(), "            ");

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n")
          .append("<!DOCTYPE mapper\n")
          .append("        PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n")
          .append("        \"https://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n")
          .append("<mapper namespace=\"com.example.sms.mapper.").append(module).append(".").append(cls).append("Mapper\">\n\n")
          .append("    <!-- 화면 date input 값은 YYYYMMDD 문자열로 전달된다 (TuiPageBuilder 규약).\n")
          .append("         비교 컬럼이 DATE/TIMESTAMP면 TO_DATE(#{변수}, 'YYYYMMDD')로 감싸서 비교한다. -->\n\n");

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
              .append("        INSERT INTO /* TODO: 테이블명 */ (REG_DTTM) VALUES (SYSTIMESTAMP)\n")
              .append("    </insert>\n\n")
              .append("    <!-- update 기준: 수정 컬럼만 SET하고, 낙관적 잠금으로 UPDATE_DTTM을 WHERE에 함께 둔다.\n")
              .append("         갱신값은 컬럼 타입 확인 후 선택한다: TIMESTAMP -> SYSTIMESTAMP, CHAR(14) -> TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS') -->\n")
              .append("    <update id=\"update\">\n")
              .append("        UPDATE /* TODO: 테이블명 */\n")
              .append("           SET /* TODO: 수정 컬럼 = #{필드}, */\n")
              .append("               UPD_DTTM = SYSTIMESTAMP\n")
              .append("         WHERE ID = #{id}\n")
              .append("           AND UPD_DTTM = #{beforeUpdateDttm}\n")
              .append("    </update>\n\n")
              .append("    <delete id=\"delete\">\n")
              .append("        DELETE FROM /* TODO: 테이블명 */ WHERE ID = #{id}\n")
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
}
