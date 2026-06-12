package com.example.sms.mapper.sms;

import com.example.sms.dto.sms.SmsHistorySearchRequestDTO;
import com.example.sms.dto.sms.SmsHistoryUpdateRequestDTO;
import com.example.sms.vo.sms.SmsHistoryVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsHistoryMapper {

    int count(SmsHistorySearchRequestDTO request);

    List<SmsHistoryVO> selectList(SmsHistorySearchRequestDTO request);

    int insert(SmsHistoryUpdateRequestDTO request);

    int update(SmsHistoryUpdateRequestDTO request);

    int delete(String id);

    // ExcelUtil 계약상 Map을 사용한다 (동적 컬럼 예외)
    List<Map<String, Object>> selectListForExcel(SmsHistorySearchRequestDTO request);
}
