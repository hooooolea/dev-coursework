package com.police.caseinfo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.caseinfo.dto.CaseQueryDTO;
import com.police.caseinfo.entity.CaseInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CaseInfoMapper extends BaseMapper<CaseInfo> {
    IPage<CaseInfo> selectCasePage(Page<CaseInfo> page, @Param("q") CaseQueryDTO query);
}
