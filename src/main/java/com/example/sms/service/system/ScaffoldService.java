package com.example.sms.service.system;

import com.example.sms.dto.system.ScaffoldRequestDTO;
import com.example.sms.dto.system.ScaffoldApplyFileResultDTO;
import com.example.sms.service.system.scaffold.ColumnTypeInferrer;
import com.example.sms.service.system.scaffold.ControllerTemplate;
import com.example.sms.service.system.scaffold.ControllerTestTemplate;
import com.example.sms.service.system.scaffold.DtoTemplate;
import com.example.sms.service.system.scaffold.HtmlTemplate;
import com.example.sms.service.system.scaffold.JsTemplate;
import com.example.sms.service.system.scaffold.MapperInterfaceTemplate;
import com.example.sms.service.system.scaffold.MapperXmlTemplate;
import com.example.sms.service.system.scaffold.MenuSqlTemplate;
import com.example.sms.service.system.scaffold.QueryColumnExtractor;
import com.example.sms.service.system.scaffold.ScaffoldFileApplier;
import com.example.sms.service.system.scaffold.ScaffoldModel;
import com.example.sms.service.system.scaffold.ServiceTemplate;
import com.example.sms.service.system.scaffold.ServiceTestTemplate;
import com.example.sms.service.system.scaffold.UpdateRequestDtoTemplate;
import com.example.sms.service.system.scaffold.VoTemplate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * QuerySpec(rawQuery + $변수)에서 화면 1세트의 코드를 생성하고,
 * local 전용 apply 요청에서는 정해진 프로젝트 경로로 저장한다.
 */
@Service
@Profile("local")
public class ScaffoldService {

    private final ColumnTypeInferrer columnTypeInferrer;
    private final ScaffoldFileApplier scaffoldFileApplier;

    public ScaffoldService(ColumnTypeInferrer columnTypeInferrer,
                           ScaffoldFileApplier scaffoldFileApplier) {
        this.columnTypeInferrer = columnTypeInferrer;
        this.scaffoldFileApplier = scaffoldFileApplier;
    }

    public Map<String, String> generate(ScaffoldRequestDTO request) {
        return generateFiles(request);
    }

    public Map<String, Object> analyze(String rawQuery) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("columns", QueryColumnExtractor.extractColumns(rawQuery));
        result.put("searchVars", QueryColumnExtractor.extractSearchVars(rawQuery));
        result.put("targetTable", QueryColumnExtractor.extractPrimaryTable(rawQuery));
        return result;
    }

    public List<ScaffoldApplyFileResultDTO> preview(ScaffoldRequestDTO request) {
        Map<String, String> generatedFiles = generateFiles(request);
        return scaffoldFileApplier.preview(request, generatedFiles);
    }

    public List<ScaffoldApplyFileResultDTO> apply(ScaffoldRequestDTO request) {
        Map<String, String> generatedFiles = generateFiles(request);
        return scaffoldFileApplier.apply(request, generatedFiles);
    }

    private Map<String, String> generateFiles(ScaffoldRequestDTO request) {
        List<String> columns = QueryColumnExtractor.extractColumns(request.getRawQuery());
        if (columns.isEmpty()) {
            throw new IllegalStateException("rawQuery에서 SELECT 컬럼을 추출하지 못했습니다. 쿼리를 확인하세요.");
        }
        List<String> searchVars = QueryColumnExtractor.extractSearchVars(request.getRawQuery());
        Map<String, String> typeMap = columnTypeInferrer.inferTypes(request.getRawQuery(), columns);

        ScaffoldModel model = new ScaffoldModel(request, columns, searchVars, typeMap);
        String cls = model.domainClass();

        Map<String, String> results = new LinkedHashMap<>();
        results.put(cls + "SearchRequestDTO.java", DtoTemplate.generate(model));
        if (model.includeCreateUpdate()) {
            results.put(cls + "UpdateRequestDTO.java", UpdateRequestDtoTemplate.generate(model));
        }
        results.put(cls + "VO.java", VoTemplate.generate(model));
        results.put(cls + "Mapper.java", MapperInterfaceTemplate.generate(model));
        results.put(cls + "Mapper.xml", MapperXmlTemplate.generate(model));
        results.put(cls + "Service.java", ServiceTemplate.generate(model));
        results.put(cls + "Controller.java", ControllerTemplate.generate(model));
        results.put(cls + "ServiceTest.java", ServiceTestTemplate.generate(model));
        results.put(cls + "ControllerTest.java", ControllerTestTemplate.generate(model));
        results.put(model.domainId() + ".html", HtmlTemplate.generate(model));
        results.put(model.domainId() + ".js", JsTemplate.generate(model));
        results.put("메뉴등록.sql", MenuSqlTemplate.generate(model));
        return results;
    }
}
