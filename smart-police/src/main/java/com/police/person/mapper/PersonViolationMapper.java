package com.police.person.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.person.entity.PersonViolation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PersonViolationMapper extends BaseMapper<PersonViolation> {
}
