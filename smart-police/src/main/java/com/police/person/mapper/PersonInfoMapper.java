package com.police.person.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.person.entity.PersonInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PersonInfoMapper extends BaseMapper<PersonInfo> {
    IPage<PersonInfo> selectPersonPage(Page<PersonInfo> page,
                                       @Param("keyword") String keyword,
                                       @Param("personType") String personType);
}
