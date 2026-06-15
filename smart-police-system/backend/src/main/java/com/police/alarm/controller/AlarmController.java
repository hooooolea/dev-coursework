package com.police.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.ai.service.AiEquipmentService;
import com.police.alarm.dto.AlarmCreateDTO;
import com.police.alarm.dto.AlarmQueryDTO;
import com.police.alarm.entity.AlarmRecord;
import com.police.alarm.service.AlarmService;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final AiEquipmentService aiEquipmentService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('alarm:view')")
    public Result<IPage<AlarmRecord>> list(AlarmQueryDTO query) {
        return Result.ok(alarmService.listPage(query));
    }

    /** 查询当前登录警员的待处置警情 */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasAuthority('alarm:view')")
    public Result<IPage<AlarmRecord>> myTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(alarmService.listMyTasks(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('alarm:view')")
    public Result<AlarmRecord> detail(@PathVariable Long id) {
        return Result.ok(alarmService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('alarm:add')")
    @OperationLog(module = "报警受理", action = "接警录入")
    public Result<Long> create(@RequestBody @Valid AlarmCreateDTO dto) {
        return Result.ok(alarmService.create(dto));
    }

    @PutMapping("/{id}/dispatch")
    @PreAuthorize("hasAuthority('alarm:dispatch')")
    @OperationLog(module = "报警受理", action = "任务派发")
    public Result<?> dispatch(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        alarmService.dispatch(id, body.get("officerId"));
        return Result.ok();
    }

    @PutMapping("/{id}/arrive")
    @PreAuthorize("hasAuthority('alarm:dispatch')")
    @OperationLog(module = "报警受理", action = "到达现场")
    public Result<?> arriveByAlarm(@PathVariable Long id) {
        alarmService.arriveByAlarm(id);
        return Result.ok();
    }

    @PutMapping("/dispatch/{dispatchId}/arrive")
    @PreAuthorize("hasAuthority('alarm:dispatch')")
    @OperationLog(module = "报警受理", action = "到达现场")
    public Result<?> arrive(@PathVariable Long dispatchId) {
        alarmService.arrive(dispatchId);
        return Result.ok();
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAuthority('alarm:close')")
    @OperationLog(module = "报警受理", action = "关闭警情")
    public Result<?> close(@PathVariable Long id, @RequestBody Map<String, String> body) {
        alarmService.close(id, body.get("summary"));
        return Result.ok();
    }

    /** 关联案件（升级为案件时调用） */
    @PutMapping("/{id}/link-case")
    @PreAuthorize("hasAuthority('alarm:edit')")
    public Result<?> linkCase(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        alarmService.linkCase(id, body.get("caseId"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('alarm:edit')")
    public Result<?> delete(@PathVariable Long id) {
        alarmService.removeById(id);
        return Result.ok();
    }

    /** AI 装备推荐（接警后自动触发） */
    @GetMapping("/{id}/equipment-recommend")
    @PreAuthorize("hasAuthority('alarm:view')")
    public Result<String> equipmentRecommend(@PathVariable Long id) {
        try {
            AlarmRecord alarm = alarmService.getById(id);
            if (alarm == null) return Result.fail(404, "警情不存在");
            String areaName = alarm.getLocationDetail() != null ? alarm.getLocationDetail() : "";
            String alarmType = alarm.getAlarmType() != null ? alarm.getAlarmType() : "";
            String result = aiEquipmentService.recommend(alarmType, "", areaName, "");
            return Result.ok(result);
        } catch (Exception e) {
            log.error("装备推荐失败 alarmId={}", id, e);
            return Result.fail("AI 服务暂不可用");
        }
    }
}
