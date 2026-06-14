package com.police.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.ai.client.MiMoClient;
import com.police.ai.util.PromptBuilder;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.mapper.CaseInfoMapper;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.mapper.OfficerInfoMapper;
import com.police.patrol.entity.PatrolSchedule;
import com.police.patrol.entity.PatrolTask;
import com.police.patrol.mapper.PatrolScheduleMapper;
import com.police.patrol.mapper.PatrolTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AI 智能排班推荐 服务
 *
 * <p>根据巡逻任务信息，综合在岗警员、已有排班、区域历史案件数据，
 * 调用 MiMo 大模型推荐最合适的执勤警员。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiScheduleService {

    private final OfficerInfoMapper officerMapper;

    private final PatrolScheduleMapper scheduleMapper;

    private final PatrolTaskMapper taskMapper;

    private final CaseInfoMapper caseMapper;

    private final MiMoClient miMoClient;

    /**
     * 为指定任务生成排班推荐
     *
     * <p>查询任务的详细信息、当前在岗警员列表、已有排班记录以及区域近期案件类型分布，
     * 构建 prompt 后通过 MiMo Pro 模型流式输出推荐结果。</p>
     *
     * @param taskId    巡逻任务 ID
     * @param onToken   token 回调，每收到一个 token 块时触发
     * @param onComplete 流结束回调
     * @throws IOException 网络或 IO 异常
     */
    public void recommend(Long taskId, Consumer<String> onToken, Runnable onComplete) throws IOException {
        PatrolTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在，taskId=" + taskId);
        }

        // 查询在岗警员
        List<OfficerInfo> officers = officerMapper.selectList(
                new LambdaQueryWrapper<OfficerInfo>()
                        .eq(OfficerInfo::getIsDeleted, 0)
                        .eq(OfficerInfo::getWorkStatus, "on_duty"));

        StringBuilder officerTable = new StringBuilder();
        for (OfficerInfo o : officers) {
            officerTable.append(String.format("%s | %s | %s | %s | - | -\n",
                    o.getBadgeNo() != null ? o.getBadgeNo() : "-",
                    o.getRealName() != null ? o.getRealName() : "-",
                    o.getPoliceCategory() != null ? o.getPoliceCategory() : "-",
                    o.getSkills() != null ? o.getSkills() : "-"));
        }

        // 查询任务当天已有排班的警员（排除已排班人员）
        LocalDate taskDate = task.getTaskStart() != null ? task.getTaskStart().toLocalDate() : LocalDate.now();
        List<PatrolSchedule> scheduled = scheduleMapper.selectList(
                new LambdaQueryWrapper<PatrolSchedule>()
                        .eq(PatrolSchedule::getScheduleDate, taskDate));
        String scheduledStr = scheduled.stream()
                .map(s -> String.valueOf(s.getOfficerId()))
                .collect(Collectors.joining(", "));

        // 查询该区域近30天案件类型分布 Top5
        String topCaseTypes = "暂无";
        if (task.getAreaName() != null && !task.getAreaName().isBlank()) {
            List<CaseInfo> areaCases = caseMapper.selectList(
                    new LambdaQueryWrapper<CaseInfo>()
                            .eq(CaseInfo::getIsDeleted, 0)
                            .like(CaseInfo::getLocationDetail, task.getAreaName())
                            .ge(CaseInfo::getFileDate, LocalDate.now().minusDays(30)));
            topCaseTypes = areaCases.stream()
                    .filter(c -> c.getCaseType() != null)
                    .collect(Collectors.groupingBy(CaseInfo::getCaseType, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> e.getKey() + "(" + e.getValue() + "起)")
                    .collect(Collectors.joining("、"));
            if (topCaseTypes.isEmpty()) {
                topCaseTypes = "暂无";
            }
        }

        String prompt = PromptBuilder.buildSchedulePrompt(
                task.getTaskType() != null ? task.getTaskType() : "例行巡逻",
                task.getAreaName() != null ? task.getAreaName() : "未指定",
                task.getTaskStart() != null ? task.getTaskStart().toString() : "",
                task.getTaskEnd() != null ? task.getTaskEnd().toString() : "",
                1,
                officerTable.toString(),
                scheduledStr,
                topCaseTypes);

        miMoClient.streamChatPro("你是一个智能警务调度助手，请基于数据推荐最合适的警员。", prompt, onToken, onComplete);
    }

    /** 无 taskId 的便捷方法（前端创建任务时调用） */
    public void recommend(String taskType, String areaName, String taskStart, String taskEnd,
                          Consumer<String> onToken, Runnable onComplete) throws IOException {
        List<OfficerInfo> officers = officerMapper.selectList(
                new LambdaQueryWrapper<OfficerInfo>()
                        .eq(OfficerInfo::getIsDeleted, 0)
                        .eq(OfficerInfo::getWorkStatus, "on_duty"));

        StringBuilder officerTable = new StringBuilder();
        for (OfficerInfo o : officers) {
            officerTable.append(String.format("%s | %s | %s | %s | - | -\n",
                    o.getBadgeNo() != null ? o.getBadgeNo() : "-",
                    o.getRealName() != null ? o.getRealName() : "-",
                    o.getPoliceCategory() != null ? o.getPoliceCategory() : "-",
                    o.getSkills() != null ? o.getSkills() : "-"));
        }

        String scheduledStr = "";
        String topCaseTypes = "暂无";
        if (areaName != null && !areaName.isBlank()) {
            List<CaseInfo> areaCases = caseMapper.selectList(
                    new LambdaQueryWrapper<CaseInfo>()
                            .eq(CaseInfo::getIsDeleted, 0)
                            .like(CaseInfo::getLocationDetail, areaName)
                            .ge(CaseInfo::getFileDate, LocalDate.now().minusDays(30)));
            topCaseTypes = areaCases.stream()
                    .filter(c -> c.getCaseType() != null)
                    .collect(Collectors.groupingBy(CaseInfo::getCaseType, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5).map(e -> e.getKey() + "(" + e.getValue() + "起)")
                    .collect(Collectors.joining("、"));
            if (topCaseTypes.isEmpty()) topCaseTypes = "暂无";
        }

        String prompt = PromptBuilder.buildSchedulePrompt(
                taskType != null ? taskType : "例行巡逻",
                areaName != null ? areaName : "未指定",
                taskStart != null ? taskStart : "",
                taskEnd != null ? taskEnd : "",
                1, officerTable.toString(), scheduledStr, topCaseTypes);

        miMoClient.streamChatPro("你是一个智能警务调度助手，请基于数据推荐最合适的警员。", prompt, onToken, onComplete);
    }
}
