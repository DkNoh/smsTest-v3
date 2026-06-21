package com.example.sms.dto.sms;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 수정 가능한 필드만 선언하는 화이트리스트 DTO.
 * TODO: 실제 수정을 허용할 필드만 남기고 제거한다.
 *       REG_ID/REG_DTTM, 시스템 필드, 권한 필드는 선언하지 않는다.
 */
@Data
public class SmsHistoryUpdateRequestDTO {

    /** PK 필드 (WHERE 조건): SMS_HISTORY_ID */
    private Integer smsHistoryId;

    private LocalDateTime sentAt;
    private String receiverNo;
    private String senderNo;
    private String sendType;
    private String sendStatus;
    private String resultCd;
    private String resultMsg;

    /** 낙관적 잠금용. 조회 시점의 UPD_DTTM (hidden으로 받는다) */
    private LocalDateTime beforeUpdDttm;
}
