package com.example.sms.mapper.menu;

import com.example.sms.vo.menu.MenuAuthVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MenuAuthMapper {

    MenuAuthVO selectMenuPermissions(@Param("menuUrl") String menuUrl,
                                     @Param("roleCodes") List<String> roleCodes);
}
