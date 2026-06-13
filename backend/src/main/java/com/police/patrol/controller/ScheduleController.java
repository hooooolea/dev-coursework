package com.police.patrol.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import com.police.patrol.entity.PatrolSchedule;
import com.police.patrol.mapper.PatrolScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@RestController
@RequestMapping("/api/patrol/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final PatrolScheduleMapper scheduleMapper;

    /** 按周查询（默认本周；传 weekOffset 偏移） */
    @GetMapping("/week")
    @PreAuthorize("hasAuthority('patrol:view')")
    public Result<Map<String, Object>> week(
            @RequestParam(defaultValue = "0") int weekOffset) {

        // 本周一
        LocalDate today    = LocalDate.now();
        LocalDate monday   = today.with(WeekFields.ISO.dayOfWeek(), 1).plusWeeks(weekOffset);
        LocalDate sunday   = monday.plusDays(6);

        List<PatrolSchedule> list = scheduleMapper.selectByDateRange(monday, sunday);

        // 按日期分组，方便前端渲染
        Map<String, List<PatrolSchedule>> byDate = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            byDate.put(monday.plusDays(i).toString(), new ArrayList<>());
        }
        for (PatrolSchedule s : list) {
            String key = s.getScheduleDate().toString();
            byDate.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weekStart", monday.toString());
        result.put("weekEnd",   sunday.toString());
        result.put("schedules", byDate);
        return Result.ok(result);
    }

    /** 按日期范围查询（扁平列表，供导出/统计用） */
    @GetMapping("/range")
    @PreAuthorize("hasAuthority('patrol:view')")
    public Result<List<PatrolSchedule>> range(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.ok(scheduleMapper.selectByDateRange(
                LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    /** 新增排班 */
    @PostMapping
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "巡逻调度", action = "新增排班")
    public Result<Long> create(@RequestBody PatrolSchedule schedule) {
        if (schedule.getStatus() == null) schedule.setStatus("normal");
        scheduleMapper.insert(schedule);
        return Result.ok(schedule.getId());
    }

    /** 批量新增（同一天多人） */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "巡逻调度", action = "批量排班")
    public Result<Integer> batchCreate(@RequestBody List<PatrolSchedule> schedules) {
        schedules.forEach(s -> { if (s.getStatus() == null) s.setStatus("normal"); });
        schedules.forEach(scheduleMapper::insert);
        return Result.ok(schedules.size());
    }

    /** 更新状态（调班/请假） */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "巡逻调度", action = "更新排班状态")
    public Result<?> updateStatus(@PathVariable Long id,
                                  @RequestBody Map<String, String> body) {
        PatrolSchedule s = new PatrolSchedule();
        s.setId(id);
        s.setStatus(body.get("status"));
        s.setRemark(body.get("remark"));
        scheduleMapper.updateById(s);
        return Result.ok();
    }

    /** 删除单条排班 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('patrol:manage')")
    @OperationLog(module = "巡逻调度", action = "删除排班")
    public Result<?> delete(@PathVariable Long id) {
        scheduleMapper.deleteById(id);
        return Result.ok();
    }
}
