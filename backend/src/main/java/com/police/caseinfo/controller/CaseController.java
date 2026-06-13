package com.police.caseinfo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.caseinfo.dto.CaseCreateDTO;
import com.police.caseinfo.dto.CaseQueryDTO;
import com.police.caseinfo.entity.CaseEvidence;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.entity.CaseProgress;
import com.police.caseinfo.entity.CaseSuspect;
import com.police.caseinfo.mapper.CaseEvidenceMapper;
import com.police.caseinfo.mapper.CaseSuspectMapper;
import com.police.caseinfo.service.CaseService;
import com.police.common.annotation.OperationLog;
import com.police.common.config.FileStorageConfig;
import com.police.common.result.Result;
import com.police.common.util.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/case")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final CaseEvidenceMapper evidenceMapper;
    private final CaseSuspectMapper suspectMapper;
    private final FileStorageService fileStorageService;
    private final FileStorageConfig fileStorageConfig;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('case:view')")
    public Result<IPage<CaseInfo>> list(CaseQueryDTO query) {
        return Result.ok(caseService.listPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('case:view')")
    public Result<CaseInfo> detail(@PathVariable Long id) {
        return Result.ok(caseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('case:add')")
    @OperationLog(module = "案件管理", action = "案件立案")
    public Result<Long> create(@RequestBody @Valid CaseCreateDTO dto) {
        return Result.ok(caseService.createCase(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "编辑案件")
    public Result<?> update(@PathVariable Long id, @RequestBody CaseInfo caseInfo) {
        caseInfo.setId(id);
        caseService.updateById(caseInfo);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "变更案件状态")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        caseService.updateStatus(id, body.get("status"), body.get("reason"));
        return Result.ok();
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('case:view')")
    public Result<List<CaseProgress>> listProgress(@PathVariable Long id) {
        return Result.ok(caseService.listProgress(id));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "新增侦查进展")
    public Result<?> addProgress(@PathVariable Long id, @RequestBody CaseProgress progress) {
        caseService.addProgress(id, progress);
        return Result.ok();
    }

    /* ====== 证据管理 ====== */

    @GetMapping("/{id}/evidence")
    @PreAuthorize("hasAuthority('case:view')")
    public Result<List<CaseEvidence>> listEvidence(@PathVariable Long id) {
        return Result.ok(evidenceMapper.selectList(
                new LambdaQueryWrapper<CaseEvidence>()
                        .eq(CaseEvidence::getCaseId, id)
                        .eq(CaseEvidence::getIsDeleted, 0)
                        .orderByAsc(CaseEvidence::getCreatedAt)));
    }

    @PostMapping("/{id}/evidence")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "新增证据")
    public Result<Long> addEvidence(@PathVariable Long id, @RequestBody CaseEvidence evidence) {
        evidence.setCaseId(id);
        evidence.setStatus(1);
        evidenceMapper.insert(evidence);
        return Result.ok(evidence.getId());
    }

    /**
     * 证据文件上传（multipart），返回已落盘的访问信息。
     *
     * <p>前端调用顺序建议：先调本接口拿到 {@code fileUrl/fileName/fileSize/fileType}，
     * 再调 {@code POST /api/case/{id}/evidence} 把 metadata（含 fileUrl）入库。</p>
     */
    @PostMapping("/{id}/evidence/upload")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "上传证据文件")
    public Result<Map<String, Object>> uploadEvidence(@PathVariable Long id,
                                                      @RequestParam("file") MultipartFile file) {
        // 校验案件存在
        CaseInfo caseInfo = caseService.getById(id);
        if (caseInfo == null) {
            return Result.fail(404, "案件不存在");
        }
        FileStorageService.StoredFile stored = fileStorageService.store(
                file, "case/" + id, fileStorageConfig.getEvidenceAllowTypes(), fileStorageConfig.getEvidenceMaxSize());
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("fileUrl", stored.getAccessUrl());
        data.put("fileName", stored.getOriginalName());
        data.put("fileSize", stored.getSize());
        data.put("fileType", stored.getExt());
        data.put("contentType", stored.getContentType());
        data.put("relativePath", stored.getRelativePath());
        return Result.ok("上传成功", data);
    }

    @DeleteMapping("/evidence/{evidenceId}")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "删除证据")
    public Result<?> deleteEvidence(@PathVariable Long evidenceId) {
        evidenceMapper.deleteById(evidenceId);
        return Result.ok();
    }

    /* ====== 嫌疑人管理 ====== */

    @GetMapping("/{id}/suspect")
    @PreAuthorize("hasAuthority('case:view')")
    public Result<List<CaseSuspect>> listSuspect(@PathVariable Long id) {
        return Result.ok(suspectMapper.selectList(
                new LambdaQueryWrapper<CaseSuspect>()
                        .eq(CaseSuspect::getCaseId, id)
                        .eq(CaseSuspect::getIsDeleted, 0)
                        .orderByAsc(CaseSuspect::getCreatedAt)));
    }

    @PostMapping("/{id}/suspect")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "新增嫌疑人")
    public Result<Long> addSuspect(@PathVariable Long id, @RequestBody CaseSuspect suspect) {
        suspect.setCaseId(id);
        suspectMapper.insert(suspect);
        return Result.ok(suspect.getId());
    }

    @PutMapping("/{id}/suspect/{suspectId}")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "更新嫌疑人")
    public Result<?> updateSuspect(@PathVariable Long id,
                                   @PathVariable Long suspectId,
                                   @RequestBody CaseSuspect suspect) {
        suspect.setId(suspectId);
        suspect.setCaseId(id);
        suspectMapper.updateById(suspect);
        return Result.ok();
    }

    @DeleteMapping("/{id}/suspect/{suspectId}")
    @PreAuthorize("hasAuthority('case:edit')")
    @OperationLog(module = "案件管理", action = "删除嫌疑人")
    public Result<?> deleteSuspect(@PathVariable Long id, @PathVariable Long suspectId) {
        suspectMapper.deleteById(suspectId);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('case:del')")
    @OperationLog(module = "案件管理", action = "删除案件")
    public Result<?> delete(@PathVariable Long id) {
        caseService.removeById(id);
        return Result.ok();
    }
}
