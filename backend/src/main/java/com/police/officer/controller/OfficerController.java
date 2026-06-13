package com.police.officer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.service.OfficerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/officer")
@RequiredArgsConstructor
public class OfficerController {

    private final OfficerService officerService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('patrol:view')")
    public Result<IPage<OfficerInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String workStatus) {
        return Result.ok(officerService.listPage(page, size, keyword, workStatus));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('patrol:view')")
    public Result<OfficerInfo> detail(@PathVariable Long id) {
        return Result.ok(officerService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "警力资源", action = "新增警员")
    public Result<Long> create(@RequestBody OfficerInfo officer) {
        officerService.save(officer);
        return Result.ok(officer.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "警力资源", action = "编辑警员")
    public Result<?> update(@PathVariable Long id, @RequestBody OfficerInfo officer) {
        officer.setId(id);
        officerService.updateById(officer);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "警力资源", action = "更新警员状态")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        OfficerInfo o = new OfficerInfo();
        o.setId(id);
        o.setWorkStatus(body.get("workStatus"));
        officerService.updateById(o);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "警力资源", action = "删除警员")
    public Result<?> delete(@PathVariable Long id) {
        officerService.removeById(id);
        return Result.ok();
    }
}
