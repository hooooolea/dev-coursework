package com.police.officer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import com.police.officer.entity.OfficerAssessment;
import com.police.officer.mapper.OfficerAssessmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/officer")
@RequiredArgsConstructor
public class AssessmentController {

    private final OfficerAssessmentMapper assessmentMapper;

    /** 查询某警员的考核记录 */
    @GetMapping("/{id}/assessments")
    @PreAuthorize("hasAuthority('patrol:view')")
    public Result<List<OfficerAssessment>> list(@PathVariable Long id) {
        return Result.ok(assessmentMapper.selectList(
                new LambdaQueryWrapper<OfficerAssessment>()
                        .eq(OfficerAssessment::getOfficerId, id)
                        .orderByDesc(OfficerAssessment::getPeriodValue)));
    }

    /** 新增考核记录，自动计算综合得分 */
    @PostMapping("/{id}/assessments")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "警力资源", action = "新增考核记录")
    public Result<Long> create(@PathVariable Long id, @RequestBody OfficerAssessment assessment) {
        assessment.setOfficerId(id);

        // 自动加总（各项可为 null，按 0 处理）
        BigDecimal total = safeAdd(
                assessment.getAttendanceScore(),
                assessment.getCaseScore(),
                assessment.getRewardScore()
        ).subtract(safe(assessment.getViolationScore()));
        assessment.setTotalScore(total);

        // 自动评定结果
        if (assessment.getResult() == null) {
            double t = total.doubleValue();
            assessment.setResult(t >= 90 ? "excellent" : t >= 75 ? "good" : t >= 60 ? "pass" : "fail");
        }

        assessmentMapper.insert(assessment);
        return Result.ok(assessment.getId());
    }

    @DeleteMapping("/assessments/{assessmentId}")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "警力资源", action = "删除考核记录")
    public Result<?> delete(@PathVariable Long assessmentId) {
        assessmentMapper.deleteById(assessmentId);
        return Result.ok();
    }

    private BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal safeAdd(BigDecimal... vals) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : vals) sum = sum.add(safe(v));
        return sum;
    }
}
