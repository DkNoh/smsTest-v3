package com.scbk.sms.mapper.sms;

import com.scbk.sms.dto.sms.SmsHistorySearchRequestDTO;
import com.scbk.sms.dto.sms.SmsHistoryUpdateRequestDTO;
import com.scbk.sms.vo.sms.SmsHistoryVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsHistoryMapper {

    int count(SmsHistorySearchRequestDTO request);

    List<SmsHistoryVO> selectList(SmsHistorySearchRequestDTO request);

    int insert(SmsHistoryUpdateRequestDTO request);

    int update(SmsHistoryUpdateRequestDTO request);

    int delete(@Param("smsHistoryId") Integer smsHistoryId, @Param("requestId") String requestId);
}
