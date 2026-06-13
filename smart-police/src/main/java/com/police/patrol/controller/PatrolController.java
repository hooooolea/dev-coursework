package com.police.patrol.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import com.police.patrol.entity.PatrolCheckin;
import com.police.patrol.entity.PatrolTask;
import com.police.patrol.service.impl.PatrolServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patrol")
@RequiredArgsConstructor
public class PatrolController {

    private final PatrolServiceImpl patrolService;

    @GetMapping("/task/list")
    @PreAuthorize("hasAuthority('patrol:view')")
    public Result<IPage<PatrolTask>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long officerId,
            @RequestParam(required = false) String status) {
        return Result.ok(patrolService.listPage(page, size, officerId, status));
    }

    @PostMapping("/task")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "巡逻调度", action = "派发任务")
    public Result<Long> createTask(@RequestBody PatrolTask task) {
        return Result.ok(patrolService.createTask(task));
    }

    @PutMapping("/task/{id}/accept")
    @OperationLog(module = "巡逻调度", action = "接收任务")
    public Result<?> accept(@PathVariable Long id) {
        patrolService.acceptTask(id);
        return Result.ok();
    }

    @PostMapping("/task/{id}/checkin")
    @OperationLog(module = "巡逻调度", action = "签到打卡")
    public Result<?> checkin(@PathVariable Long id, @RequestBody Map<String, String> body) {
        patrolService.checkin(id, body.get("type"), body.get("note"));
        return Result.ok();
    }

    @GetMapping("/task/{id}/checkins")
    public Result<List<PatrolCheckin>> checkins(@PathVariable Long id) {
        return Result.ok(patrolService.listCheckins(id));
    }

    @PutMapping("/task/{id}/complete")
    @OperationLog(module = "巡逻调度", action = "完成任务")
    public Result<?> complete(@PathVariable Long id, @RequestBody Map<String, String> body) {
        patrolService.completeTask(id, body.get("summary"));
        return Result.ok();
    }

    @DeleteMapping("/task/{id}")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "巡逻调度", action = "删除任务")
    public Result<?> delete(@PathVariable Long id) {
        patrolService.removeById(id);
        return Result.ok();
    }
}
