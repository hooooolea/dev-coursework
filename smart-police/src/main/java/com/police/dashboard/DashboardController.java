package com.police.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.alarm.entity.AlarmRecord;
import com.police.alarm.mapper.AlarmRecordMapper;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.mapper.CaseInfoMapper;
import com.police.common.result.Result;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.mapper.OfficerInfoMapper;
import com.police.system.entity.SysDict;
import com.police.system.mapper.SysDictMapper;
import com.police.vehicle.entity.VehicleControl;
import com.police.vehicle.mapper.VehicleControlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AlarmRecordMapper alarmMapper;
    private final CaseInfoMapper caseMapper;
    private final VehicleControlMapper vehicleControlMapper;
    private final OfficerInfoMapper officerMapper;
    private final SysDictMapper dictMapper;

    /** 四个统计卡片 + 7天趋势 + 案件类型分布 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = todayStart.plusDays(1);

        // 今日接警
        long todayAlarm = alarmMapper.selectCount(
                new LambdaQueryWrapper<AlarmRecord>()
                        .eq(AlarmRecord::getIsDeleted, 0)
                        .ge(AlarmRecord::getAlarmTime, todayStart)
                        .lt(AlarmRecord::getAlarmTime, todayEnd));

        // 在办案件（pending / investigating）
        long activeCase = caseMapper.selectCount(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .in(CaseInfo::getStatus, Arrays.asList("pending", "investigating")));

        // 布控车辆
        long controlledVehicle = vehicleControlMapper.selectCount(
                new LambdaQueryWrapper<VehicleControl>()
                        .eq(VehicleControl::getStatus, 1));

        // 在岗警员
        long onDutyOfficer = officerMapper.selectCount(
                new LambdaQueryWrapper<OfficerInfo>()
                        .eq(OfficerInfo::getIsDeleted, 0)
                        .eq(OfficerInfo::getWorkStatus, "on_duty"));

        // 近7天接警趋势
        LocalDateTime sevenDaysAgo = todayStart.minusDays(6);
        List<AlarmRecord> recentAlarms = alarmMapper.selectList(
                new LambdaQueryWrapper<AlarmRecord>()
                        .eq(AlarmRecord::getIsDeleted, 0)
                        .ge(AlarmRecord::getAlarmTime, sevenDaysAgo));

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("M/d");
        List<String> trendDates = new ArrayList<>();
        List<Long> trendCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            trendDates.add(day.format(labelFmt));
            long cnt = recentAlarms.stream()
                    .filter(a -> a.getAlarmTime() != null
                            && a.getAlarmTime().toLocalDate().equals(day))
                    .count();
            trendCounts.add(cnt);
        }

        // 案件类型分布（取最近全部未删除案件，按 caseType 聚合）
        List<CaseInfo> allCases = caseMapper.selectList(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .isNotNull(CaseInfo::getCaseType));
        // 案件类型字典（英文→中文）
        Map<String, String> typeDict = dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getDictType, "case_type")
                        .eq(SysDict::getStatus, 1))
                .stream().collect(Collectors.toMap(SysDict::getDictValue, SysDict::getDictLabel, (a, b) -> a));

        Map<String, Long> typeMap = allCases.stream()
                .collect(Collectors.groupingBy(c -> c.getCaseType() == null ? "其他" : c.getCaseType(),
                        Collectors.counting()));
        List<Map<String, Object>> typeStats = typeMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", typeDict.getOrDefault(e.getKey(), e.getKey()));
                    item.put("value", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("todayAlarm",        todayAlarm);
        data.put("activeCase",        activeCase);
        data.put("controlledVehicle", controlledVehicle);
        data.put("onDutyOfficer",     onDutyOfficer);
        data.put("trendDates",        trendDates);
        data.put("trendCounts",       trendCounts);
        data.put("typeStats",         typeStats);

        return Result.ok(data);
    }
}
