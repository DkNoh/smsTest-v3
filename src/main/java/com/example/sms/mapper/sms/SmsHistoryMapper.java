package com.example.sms.mapper.sms;

import com.example.sms.dto.sms.SmsHistorySearchRequestDTO;
import com.example.sms.dto.sms.SmsHistoryUpdateRequestDTO;
import com.example.sms.vo.sms.SmsHistoryVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsHistoryMapper {

    int count(SmsHistorySearchRequestDTO request);

    List<SmsHistoryVO> selectList(SmsHistorySearchRequestDTO request);

    int insert(SmsHistoryUpdateRequestDTO request);

    int update(SmsHistoryUpdateRequestDTO request);

    int delete(Integer smsHistoryId);
}
