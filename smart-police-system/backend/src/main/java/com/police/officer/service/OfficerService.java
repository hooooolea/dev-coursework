package com.police.officer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.officer.entity.OfficerInfo;

public interface OfficerService extends IService<OfficerInfo> {
    IPage<OfficerInfo> listPage(int page, int size, String keyword, String workStatus);
}
