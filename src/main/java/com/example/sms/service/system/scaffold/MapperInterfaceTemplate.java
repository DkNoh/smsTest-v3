package com.example.sms.service.system.scaffold;

/** Mapper interface 생성. */
public final class MapperInterfaceTemplate {

    private MapperInterfaceTemplate() {
    }

    public static String generate(ScaffoldModel model) {
        String cls = model.domainClass();
        StringBuilder sb = new StringBuilder();
        sb.append("package com.example.sms.mapper.").append(model.moduleName()).append(";\n\n")
          .append("import com.example.sms.dto.").append(model.moduleName()).append(".").append(cls).append("SearchRequestDTO;\n");
        if (model.includeCreateUpdate()) {
            sb.append("import com.example.sms.dto.").append(model.moduleName()).append(".").append(cls).append("UpdateRequestDTO;\n");
        }
        sb.append("import com.example.sms.vo.").append(model.moduleName()).append(".").append(cls).append("VO;\n")
          .append("import java.util.List;\n");
        if (model.includeExcel()) {
            sb.append("import java.util.Map;\n");
        }
        sb.append("import org.apache.ibatis.annotations.Mapper;\n\n")
          .append("@Mapper\n")
          .append("public interface ").append(cls).append("Mapper {\n\n")
          .append("    int count(").append(cls).append("SearchRequestDTO request);\n\n")
          .append("    List<").append(cls).append("VO> selectList(").append(cls).append("SearchRequestDTO request);\n");

        if (model.includeCreateUpdate()) {
            sb.append("\n    int insert(").append(cls).append("UpdateRequestDTO request);\n\n")
              .append("    int update(").append(cls).append("UpdateRequestDTO request);\n\n")
              .append("    int delete(String id);\n");
        }
        if (model.includeExcel()) {
            sb.append("\n    // ExcelUtil 계약상 Map을 사용한다 (동적 컬럼 예외)\n")
              .append("    List<Map<String, Object>> selectListForExcel(").append(cls).append("SearchRequestDTO request);\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
