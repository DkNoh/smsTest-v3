package com.scbk.sms.service.sms;

import com.scbk.sms.dto.common.PageResponseDTO;
import com.scbk.sms.dto.sms.SmsHistorySearchRequestDTO;
import com.scbk.sms.dto.sms.SmsHistoryUpdateRequestDTO;
import com.scbk.sms.exception.CustomException;
import com.scbk.sms.exception.ErrorCode;
import com.scbk.sms.mapper.sms.SmsHistoryMapper;
import com.scbk.sms.vo.sms.SmsHistoryVO;
import java.util.List;
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
    public void delete(Integer smsHistoryId, String requestId) {
        int deleted = mapper.delete(smsHistoryId, requestId);
        if (deleted == 0) {
            // 다른 사용자가 먼저 삭제했거나 대상이 없다
            throw new CustomException(ErrorCode.DELETE_CONFLICT);
        }
    }
}
