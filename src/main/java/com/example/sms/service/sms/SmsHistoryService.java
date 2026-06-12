package com.example.sms.service.sms;

import com.example.sms.dto.common.PageResponseDTO;
import com.example.sms.dto.sms.SmsHistorySearchRequestDTO;
import com.example.sms.dto.sms.SmsHistoryUpdateRequestDTO;
import com.example.sms.exception.CustomException;
import com.example.sms.exception.ErrorCode;
import com.example.sms.mapper.sms.SmsHistoryMapper;
import com.example.sms.vo.sms.SmsHistoryVO;
import com.example.sms.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SmsHistoryService {

    private final SmsHistoryMapper mapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<SmsHistoryVO> search(SmsHistorySearchRequestDTO request) {
        request.validate();
        int totalCount = mapper.count(request);
        List<SmsHistoryVO> list = mapper.selectList(request);
        return PageResponseDTO.of(list, request, totalCount);
    }

    @Transactional
    public void create(SmsHistoryUpdateRequestDTO request) {
        mapper.insert(request);
    }

    @Transactional
    public void update(SmsHistoryUpdateRequestDTO request) {
        int updated = mapper.update(request);
        if (updated == 0) {
            // 다른 사용자가 먼저 수정했거나(낙관적 잠금) 대상이 없다
            throw new CustomException(ErrorCode.UPDATE_CONFLICT);
        }
    }

    @Transactional
    public void delete(String id) {
        mapper.delete(id);
    }

    @Transactional(readOnly = true)
    public void downloadExcel(SmsHistorySearchRequestDTO request, HttpServletResponse response) {
        String[] headers = { "SENT_AT", "RECEIVER_NO", "SEND_TYPE" };
        String[] keys = { "SENT_AT", "RECEIVER_NO", "SEND_TYPE" };
        List<Map<String, Object>> list = mapper.selectListForExcel(request);
        ExcelUtil.downloadExcel(response, "SmsHistory_export", headers, list, keys);
    }
}
