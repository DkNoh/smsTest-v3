package com.scbk.sms.dto.sms;

import com.scbk.sms.dto.common.PageRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SmsHistorySearchRequestDTO extends PageRequestDTO {

    private String sendType;
    private String sendStatus;
    private String sentAt;
    private String receiverNo;
}
