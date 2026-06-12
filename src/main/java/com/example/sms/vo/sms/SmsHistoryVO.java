package com.example.sms.vo.sms;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SmsHistoryVO {

    private long rowNum;
    private LocalDateTime sentAt;
    private String receiverNo;
    private String sendType;
}
