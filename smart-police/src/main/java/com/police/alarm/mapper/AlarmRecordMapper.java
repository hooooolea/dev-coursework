package com.police.alarm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.alarm.dto.AlarmQueryDTO;
import com.police.alarm.entity.AlarmRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlarmRecordMapper extends BaseMapper<AlarmRecord> {
    IPage<AlarmRecord> selectPage(Page<AlarmRecord> page, @Param("q") AlarmQueryDTO query);
}
