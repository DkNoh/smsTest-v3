package com.example.sms.mapper.auth;

import com.example.sms.vo.auth.LoginEmployeeVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginEmployeeMapper {

    List<LoginEmployeeVO> selectActiveEmployeesByEmpId(@Param("empId") String empId);
}
