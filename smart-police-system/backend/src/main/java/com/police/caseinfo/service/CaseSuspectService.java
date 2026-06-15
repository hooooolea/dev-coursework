package com.police.caseinfo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.caseinfo.entity.CaseSuspect;
import com.police.caseinfo.mapper.CaseSuspectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseSuspectService {

    private final CaseSuspectMapper suspectMapper;

    public List<CaseSuspect> listByCaseId(Long caseId) {
        return suspectMapper.selectList(
                new LambdaQueryWrapper<CaseSuspect>()
                        .eq(CaseSuspect::getCaseId, caseId)
                        .orderByAsc(CaseSuspect::getCreatedAt));
    }

    public CaseSuspect getById(Long id) {
        return suspectMapper.selectById(id);
    }

    public Long create(CaseSuspect suspect) {
        suspectMapper.insert(suspect);
        return suspect.getId();
    }

    public void update(CaseSuspect suspect) {
        suspectMapper.updateById(suspect);
    }

    public void delete(Long id) {
        suspectMapper.deleteById(id);
    }
}
