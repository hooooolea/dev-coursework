package com.police.caseinfo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.caseinfo.dto.CaseCreateDTO;
import com.police.caseinfo.dto.CaseQueryDTO;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.entity.CaseProgress;

import java.util.List;

public interface CaseService extends IService<CaseInfo> {
    Long createCase(CaseCreateDTO dto);
    IPage<CaseInfo> listPage(CaseQueryDTO query);
    void updateStatus(Long id, String status, String reason);
    void addProgress(Long caseId, CaseProgress progress);
    List<CaseProgress> listProgress(Long caseId);
}
