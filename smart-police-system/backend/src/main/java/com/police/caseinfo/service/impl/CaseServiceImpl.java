package com.police.caseinfo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.caseinfo.dto.CaseCreateDTO;
import com.police.caseinfo.dto.CaseQueryDTO;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.entity.CaseProgress;
import com.police.caseinfo.mapper.CaseInfoMapper;
import com.police.caseinfo.mapper.CaseProgressMapper;
import com.police.caseinfo.service.CaseService;
import com.police.common.exception.BusinessException;
import com.police.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CaseServiceImpl extends ServiceImpl<CaseInfoMapper, CaseInfo> implements CaseService {

    private final CaseProgressMapper progressMapper;
    private final AtomicInteger seqCounter = new AtomicInteger(0);

    @Override
    public Long createCase(CaseCreateDTO dto) {
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseNo(generateCaseNo());
        caseInfo.setCaseName(dto.getCaseName());
        caseInfo.setCaseCategory(dto.getCaseCategory());
        caseInfo.setCaseType(dto.getCaseType());
        caseInfo.setOccurredAt(dto.getOccurredAt());
        caseInfo.setLocationProvince(dto.getLocationProvince());
        caseInfo.setLocationCity(dto.getLocationCity());
        caseInfo.setLocationDistrict(dto.getLocationDistrict());
        caseInfo.setLocationDetail(dto.getLocationDetail());
        caseInfo.setCaseDesc(dto.getCaseDesc());
        caseInfo.setStatus("investigating");
        caseInfo.setSeverityLevel(dto.getSeverityLevel());
        caseInfo.setLeadOfficerId(dto.getLeadOfficerId() != null
                ? dto.getLeadOfficerId() : SecurityUtil.getCurrentUserId());
        caseInfo.setDeptId(dto.getDeptId());
        caseInfo.setFileDate(LocalDate.now());
        caseInfo.setDeadlineDate(dto.getDeadlineDate());
        caseInfo.setRelatedAlarmId(dto.getRelatedAlarmId());
        caseInfo.setRemark(dto.getRemark());
        caseInfo.setIsOverdue(0);
        save(caseInfo);
        return caseInfo.getId();
    }

    @Override
    public IPage<CaseInfo> listPage(CaseQueryDTO query) {
        Page<CaseInfo> page = new Page<>(query.getPage(), query.getSize());
        com.police.system.entity.SysUser user = SecurityUtil.getCurrentUser();
        if (user != null && user.getRoleCodes() != null
            && !user.getRoleCodes().contains("ROLE_SUPER_ADMIN")
            && !user.getRoleCodes().contains("ROLE_DIRECTOR")) {
            query.setLeadOfficerId(user.getId());
        }
        return baseMapper.selectCasePage(page, query);
    }

    @Override
    public void updateStatus(Long id, String status, String reason) {
        CaseInfo caseInfo = getById(id);
        if (caseInfo == null) throw BusinessException.of("案件不存在");
        caseInfo.setStatus(status);
        if ("closed".equals(status)) {
            caseInfo.setClosedDate(LocalDate.now());
        }
        if ("cancelled".equals(status)) {
            caseInfo.setCancelledReason(reason);
        }
        updateById(caseInfo);
    }

    @Override
    public void addProgress(Long caseId, CaseProgress progress) {
        if (getById(caseId) == null) throw BusinessException.of("案件不存在");
        progress.setCaseId(caseId);
        progress.setOfficerId(SecurityUtil.getCurrentUserId());
        progress.setProgressTime(LocalDateTime.now());
        progressMapper.insert(progress);
    }

    @Override
    public List<CaseProgress> listProgress(Long caseId) {
        return progressMapper.selectList(
                new LambdaQueryWrapper<CaseProgress>()
                        .eq(CaseProgress::getCaseId, caseId)
                        .orderByDesc(CaseProgress::getProgressTime));
    }

    private String generateCaseNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "AJ" + date;
        List<CaseInfo> today = baseMapper.selectList(
            new LambdaQueryWrapper<CaseInfo>().likeRight(CaseInfo::getCaseNo, prefix).orderByDesc(CaseInfo::getCaseNo).last("LIMIT 1"));
        int maxSeq = 0;
        if (!today.isEmpty()) {
            String last = today.get(0).getCaseNo();
            try { maxSeq = Integer.parseInt(last.substring(last.length() - 3)); } catch (Exception ignored) {}
        }
        int seq = Math.max(seqCounter.incrementAndGet(), maxSeq + 1) % 1000;
        return prefix + String.format("%03d", seq);
    }
}
