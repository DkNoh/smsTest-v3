package com.example.sms.service.menu;

import java.util.List;

public interface RoleProvider {

    List<String> getActiveRoleCodes(String empId, String depId);
}