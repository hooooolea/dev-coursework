package com.police.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.ai.client.MiMoClient;
import com.police.alarm.entity.AlarmRecord;
import com.police.alarm.mapper.AlarmRecordMapper;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.mapper.CaseInfoMapper;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.mapper.OfficerInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AI 警情研判 + 通用对话服务。
 * 采集近期警务数据构建上下文，交由 MiMo 模型分析。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final AlarmRecordMapper alarmMapper;
    private final CaseInfoMapper caseMapper;
    private final OfficerInfoMapper officerMapper;
    private final MiMoClient miMoClient;

    /** 警情研判：汇总近30天数据，流式输出 AI 分析 */
    public void analyzeRecent(String focus, Consumer<String> onToken, Runnable onComplete) throws IOException {
        LocalDate since = LocalDate.now().minusDays(30);

        List<AlarmRecord> alarms = alarmMapper.selectList(
                new LambdaQueryWrapper<AlarmRecord>()
                        .eq(AlarmRecord::getIsDeleted, 0)
                        .ge(AlarmRecord::getAlarmTime, since.atStartOfDay()));

        Map<String, Long> alarmByType = alarms.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAlarmType() != null ? a.getAlarmType() : "其他",
                        Collectors.counting()));

        Map<String, Long> alarmByDistrict = alarms.stream()
                .filter(a -> a.getLocationDistrict() != null)
                .collect(Collectors.groupingBy(AlarmRecord::getLocationDistrict, Collectors.counting()));

        List<CaseInfo> cases = caseMapper.selectList(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .ge(CaseInfo::getFileDate, since));
        long investigating = cases.stream().filter(c -> "investigating".equals(c.getStatus())).count();
        long closed        = cases.stream().filter(c -> "closed".equals(c.getStatus())).count();

        long onDuty = officerMapper.selectCount(
                new LambdaQueryWrapper<OfficerInfo>()
                        .eq(OfficerInfo::getIsDeleted, 0)
                        .eq(OfficerInfo::getWorkStatus, "on_duty"));

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("近30天：接警%d起，立案%d件（侦查中%d件/已结案%d件），在岗警员%d人。\n",
                alarms.size(), cases.size(), investigating, closed, onDuty));
        summary.append("警情类型：").append(alarmByType.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> e.getKey() + e.getValue() + "起")
                .collect(Collectors.joining("，"))).append("。\n");
        summary.append("区域Top5：").append(alarmByDistrict.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5).map(e -> e.getKey() + e.getValue() + "起")
                .collect(Collectors.joining("，"))).append("。");
        if (focus != null && !focus.isBlank()) {
            summary.append("\n用户关注：").append(focus);
        }

        String prompt = "你是警务数据分析专家。请基于以下近期警情数据进行研判分析。\n"
                + "请分析：1.近期警情规律和趋势 2.高风险区域和时段 3.针对性预防建议。\n"
                + summary;

        miMoClient.streamChatPro("你是警务数据分析专家，请用中文回答，给出可操作的警务建议。",
                prompt, onToken, onComplete);
    }

    /** 通用 AI 对话：构建当前系统概况上下文，让 AI 能回答警务相关问题 */
    public void generalChat(String userMessage, Consumer<String> onToken, Runnable onComplete) throws IOException {
        LocalDate today = LocalDate.now();

        long todayAlarms = alarmMapper.selectCount(
                new LambdaQueryWrapper<AlarmRecord>()
                        .eq(AlarmRecord::getIsDeleted, 0)
                        .ge(AlarmRecord::getAlarmTime, today.atStartOfDay()));

        long activeCases = caseMapper.selectCount(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .eq(CaseInfo::getStatus, "investigating"));

        long onDuty = officerMapper.selectCount(
                new LambdaQueryWrapper<OfficerInfo>()
                        .eq(OfficerInfo::getIsDeleted, 0)
                        .eq(OfficerInfo::getWorkStatus, "on_duty"));

        String context = String.format(
                "当前系统实时数据：今日接警%d起，在办案件%d件，在岗警员%d人。当前日期：%s。",
                todayAlarms, activeCases, onDuty, today);

        String systemPrompt = "你是智能警务助手，集成在警务管理系统中。"
                + "你可以基于以下实时数据回答用户的警务相关问题。"
                + "如果用户的问题超出数据范围，请诚实告知并在力所能及范围内提供帮助。"
                + "请用简洁、专业、友好的中文回答。\n"
                + context;

        miMoClient.streamChatPro(systemPrompt, userMessage, onToken, onComplete);
    }
}
