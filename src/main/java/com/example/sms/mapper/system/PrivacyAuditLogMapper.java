package com.example.sms.mapper.system;

import com.example.sms.vo.system.PrivacyAuditLogVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrivacyAuditLogMapper {

    int insertAuditLog(PrivacyAuditLogVO logVO);
}
