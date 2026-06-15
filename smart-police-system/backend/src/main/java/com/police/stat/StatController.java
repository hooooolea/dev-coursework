package com.police.stat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.alarm.entity.AlarmRecord;
import com.police.alarm.mapper.AlarmRecordMapper;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.mapper.CaseInfoMapper;
import com.police.common.result.Result;
import com.police.system.entity.SysDict;
import com.police.system.mapper.SysDictMapper;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.mapper.OfficerInfoMapper;
import com.police.patrol.entity.PatrolTask;
import com.police.patrol.mapper.PatrolTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor
public class StatController {

    private final CaseInfoMapper caseMapper;
    private final AlarmRecordMapper alarmMapper;
    private final OfficerInfoMapper officerMapper;
    private final PatrolTaskMapper patrolTaskMapper;
    private final SysDictMapper dictMapper;

    /**
     * 案件统计：近N个月立案数（折线）+ 按类型分布（饼）+ 破案率 + 环比/同比
     * ?months=6
     */
    @GetMapping("/case")
    public Result<Map<String, Object>> caseStats(@RequestParam(defaultValue = "6") int months) {
        LocalDate now = LocalDate.now();
        LocalDate currentStart = now.withDayOfMonth(1).minusMonths(months - 1);
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        // ===== 当前周期数据 =====
        List<CaseInfo> all = caseMapper.selectList(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .ge(CaseInfo::getFileDate, currentStart));

        // 按月立案数
        Map<String, Long> byMonth = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) {
            byMonth.put(now.minusMonths(i).format(monthFmt), 0L);
        }
        all.stream()
                .filter(c -> c.getFileDate() != null)
                .forEach(c -> byMonth.merge(c.getFileDate().format(monthFmt), 1L, Long::sum));

        // 案件类型字典（英文→中文）
        Map<String, String> caseTypeDict = dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getDictType, "case_type")
                        .eq(SysDict::getStatus, 1))
                .stream().collect(Collectors.toMap(SysDict::getDictValue, SysDict::getDictLabel, (a, b) -> a));

        // 按案件类型分布
        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCaseType() == null ? "其他" : c.getCaseType(),
                        Collectors.counting()));
        List<Map<String, Object>> typeStats = byType.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    String label = caseTypeDict.getOrDefault(e.getKey(), e.getKey());
                    m.put("name", label); m.put("value", e.getValue());
                    return m;
                }).collect(Collectors.toList());

        long total    = all.stream().filter(c -> !"cancelled".equals(c.getStatus())).count();
        long closed   = all.stream().filter(c -> "closed".equals(c.getStatus())).count();
        double solveRate = total == 0 ? 0 : Math.round(closed * 1000.0 / total) / 10.0;

        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus() == null ? "unknown" : c.getStatus(), Collectors.counting()));

        // ===== 环比（上N个月同期） =====
        LocalDate momStart = currentStart.minusMonths(months);
        LocalDate momEnd   = currentStart.minusDays(1);
        Map<String, Object> mom = computeComparison(momStart, momEnd);

        // ===== 同比（去年同N个月） =====
        LocalDate yoyStart = currentStart.minusYears(1);
        LocalDate yoyEnd   = now.minusYears(1).withDayOfMonth(1).plusMonths(months - 1)
                .withDayOfMonth(now.minusYears(1).lengthOfMonth());
        Map<String, Object> yoy = computeComparison(yoyStart, yoyEnd);

        // ===== 组装响应 =====
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("months",        new ArrayList<>(byMonth.keySet()));
        data.put("monthlyCounts", new ArrayList<>(byMonth.values()));
        data.put("typeStats",     typeStats);
        data.put("total",         all.size());
        data.put("closed",        closed);
        data.put("solveRate",     solveRate);
        data.put("byStatus",      byStatus);
        data.put("mom",           mom);
        data.put("yoy",           yoy);

        return Result.ok(data);
    }

    /** 查询某个时间段的数据，返回 { total, closed, solveRate } */
    private Map<String, Object> computeComparison(LocalDate start, LocalDate end) {
        if (end == null) end = LocalDate.now();
        List<CaseInfo> periodCases = caseMapper.selectList(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .ge(CaseInfo::getFileDate, start)
                        .le(CaseInfo::getFileDate, end));

        long periodTotal  = periodCases.stream().filter(c -> !"cancelled".equals(c.getStatus())).count();
        long periodClosed = periodCases.stream().filter(c -> "closed".equals(c.getStatus())).count();
        double periodSolveRate = periodTotal == 0 ? 0 : Math.round(periodClosed * 1000.0 / periodTotal) / 10.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total",     periodTotal);
        result.put("closed",    periodClosed);
        result.put("solveRate", periodSolveRate);
        return result;
    }

    /** 警员近30天工作量排行 */
    @GetMapping("/officer-workload")
    public Result<List<Map<String, Object>>> officerWorkload() {
        java.time.LocalDate since = java.time.LocalDate.now().minusDays(30);
        List<OfficerInfo> officers = officerMapper.selectList(
            new LambdaQueryWrapper<OfficerInfo>().eq(OfficerInfo::getIsDeleted, 0));
        List<PatrolTask> tasks = patrolTaskMapper.selectList(
            new LambdaQueryWrapper<PatrolTask>().ge(PatrolTask::getCreatedAt, since.atStartOfDay()));
        Map<Long, Long> patrolByOfficer = tasks.stream()
            .collect(Collectors.groupingBy(PatrolTask::getOfficerId, Collectors.counting()));
        List<Map<String, Object>> result = officers.stream().map(o -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("officerId", o.getId());
            item.put("name", o.getRealName());
            item.put("badgeNo", o.getBadgeNo());
            item.put("patrols", patrolByOfficer.getOrDefault(o.getId(), 0L));
            item.put("workload", patrolByOfficer.getOrDefault(o.getId(), 0L));
            return item;
        }).sorted((a, b) -> Long.compare((Long) b.get("workload"), (Long) a.get("workload")))
          .collect(Collectors.toList());
        return Result.ok(result);
    }

    /**
     * 警力效能统计
     */
    @GetMapping("/officer")
    public Result<Map<String, Object>> officerStats() {
        List<OfficerInfo> officers = officerMapper.selectList(
                new LambdaQueryWrapper<OfficerInfo>().eq(OfficerInfo::getIsDeleted, 0));

        long total    = officers.size();
        long onDuty   = officers.stream().filter(o -> "on_duty".equals(o.getWorkStatus())).count();
        long vacation = officers.stream().filter(o -> "vacation".equals(o.getWorkStatus())).count();
        long business = officers.stream().filter(o -> "business".equals(o.getWorkStatus())).count();

        // 各警员巡逻任务完成数（近30天）
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<PatrolTask> recentTasks = patrolTaskMapper.selectList(
                new LambdaQueryWrapper<PatrolTask>()
                        .eq(PatrolTask::getStatus, "completed")
                        .ge(PatrolTask::getCompleteTime, thirtyDaysAgo.atStartOfDay()));

        Map<Long, Long> tasksByOfficer = recentTasks.stream()
                .collect(Collectors.groupingBy(PatrolTask::getOfficerId, Collectors.counting()));

        // 各警员30天内负责的案件数
        List<CaseInfo> recentCases = caseMapper.selectList(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .ge(CaseInfo::getFileDate, thirtyDaysAgo));
        Map<Long, Long> casesByOfficer = recentCases.stream()
                .filter(c -> c.getLeadOfficerId() != null)
                .collect(Collectors.groupingBy(CaseInfo::getLeadOfficerId, Collectors.counting()));

        // 合并为警员效能列表（取前10）
        List<Map<String, Object>> officerList = officers.stream()
                .sorted(Comparator.comparingLong(
                        o -> -(casesByOfficer.getOrDefault(o.getId(), 0L) + tasksByOfficer.getOrDefault(o.getId(), 0L))))
                .limit(10)
                .map(o -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id",         o.getId());
                    item.put("name",       o.getRealName());
                    item.put("badgeNo",    o.getBadgeNo());
                    item.put("workStatus", o.getWorkStatus());
                    item.put("cases30d",   casesByOfficer.getOrDefault(o.getId(), 0L));
                    item.put("patrols30d", tasksByOfficer.getOrDefault(o.getId(), 0L));
                    return item;
                }).collect(Collectors.toList());

        // 状态分布（饼图用）
        List<Map<String, Object>> statusDist = List.of(
                Map.of("name", "在岗", "value", onDuty),
                Map.of("name", "休假", "value", vacation),
                Map.of("name", "外出", "value", business),
                Map.of("name", "其他", "value", total - onDuty - vacation - business)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total",      total);
        data.put("onDuty",     onDuty);
        data.put("onDutyRate", total == 0 ? 0 : Math.round(onDuty * 1000.0 / total) / 10.0);
        data.put("statusDist", statusDist);
        data.put("officerList", officerList);

        return Result.ok(data);
    }
}
