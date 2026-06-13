package com.police.caseinfo.controller;

import com.police.caseinfo.entity.CaseSuspect;
import com.police.caseinfo.service.CaseSuspectService;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/case/{caseId}/suspect")
@RequiredArgsConstructor
public class CaseSuspectController {

    private final CaseSuspectService suspectService;

    @GetMapping
    @PreAuthorize("hasAuthority('case:view')")
    public Result<List<CaseSuspect>> list(@PathVariable Long caseId) {
        return Result.ok(suspectService.listByCaseId(caseId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('case:view')")
    public Result<CaseSuspect> detail(@PathVariable Long caseId, @PathVariable Long id) {
        return Result.ok(suspectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "新增嫌疑人")
    public Result<Long> create(@PathVariable Long caseId, @RequestBody CaseSuspect suspect) {
        suspect.setCaseId(caseId);
        return Result.ok(suspectService.create(suspect));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "更新嫌疑人")
    public Result<?> update(@PathVariable Long caseId, @PathVariable Long id, @RequestBody CaseSuspect suspect) {
        suspect.setId(id);
        suspect.setCaseId(caseId);
        suspectService.update(suspect);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "删除嫌疑人")
    public Result<?> delete(@PathVariable Long caseId, @PathVariable Long id) {
        suspectService.delete(id);
        return Result.ok();
    }
}
