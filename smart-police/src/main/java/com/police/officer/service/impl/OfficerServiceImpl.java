package com.police.officer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.mapper.OfficerInfoMapper;
import com.police.officer.service.OfficerService;
import org.springframework.stereotype.Service;

@Service
public class OfficerServiceImpl extends ServiceImpl<OfficerInfoMapper, OfficerInfo>
        implements OfficerService {

    @Override
    public IPage<OfficerInfo> listPage(int page, int size, String keyword, String workStatus) {
        LambdaQueryWrapper<OfficerInfo> q = new LambdaQueryWrapper<OfficerInfo>()
                .eq(OfficerInfo::getIsDeleted, 0)
                .orderByAsc(OfficerInfo::getBadgeNo);
        if (keyword != null && !keyword.isBlank())
            q.and(w -> w.like(OfficerInfo::getRealName, keyword)
                        .or().like(OfficerInfo::getBadgeNo, keyword));
        if (workStatus != null && !workStatus.isBlank())
            q.eq(OfficerInfo::getWorkStatus, workStatus);
        return page(new Page<>(page, size), q);
    }
}
