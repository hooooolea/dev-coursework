package com.police.ai.util;

/**
 * AI Prompt 模板构建工具。
 *
 * <p>将结构化业务数据拼接为 MiMo 大模型可理解的 prompt 文本，统一管理各场景的 prompt 格式。
 * 核心原则：上下文压缩到 800 token 以内，仅输出必要字段。</p>
 *
 * @author smart-police
 */
public class PromptBuilder {

    /**
     * 构建智能排班推荐 prompt。
     *
     * <p>根据任务类型、区域、时段、紧急程度等信息，结合当前在岗警员和该区域历史案件分布，
     * 推荐最合适的执勤警员。</p>
     *
     * @param taskType         任务类型（例行巡逻 / 专项检查 / 定点值守）
     * @param areaName         巡逻区域名称
     * @param taskStart        任务开始时间
     * @param taskEnd          任务结束时间
     * @param urgency          紧急程度（1-4级）
     * @param officerTable     当前在岗警员列表（表格格式）
     * @param scheduledOfficers 该时段已排班警员（排除名单）
     * @param areaCaseTypes    该区域近30天主要案件类型 Top5
     * @return AI prompt 字符串
     */
    public static String buildSchedulePrompt(String taskType, String areaName,
            String taskStart, String taskEnd, int urgency,
            String officerTable, String scheduledOfficers, String areaCaseTypes) {
        return String.format("""
                你是一个智能警务调度助手，请基于以下实时数据为即将派发的巡逻任务推荐最合适的警员。

                【任务信息】
                类型：%s（例行巡逻/专项检查/定点值守）
                区域：%s
                时间：%s ~ %s
                紧急程度：%d（1-4级）

                【当前在岗警员】（共若干人，格式：警号 | 姓名 | 类别 | 擅长 | 近30天任务数 | 距上次执勤天数）
                %s

                【该时段已排班警员（排除）】
                %s

                【该区域近30天主要案件类型 Top5】
                %s

                请以 JSON 格式回答（仅输出 JSON，不含其他文字）：
                {"recommended":[{"officerId":数字,"name":"姓名","reason":"理由"}],"count":建议人数,"summary":"整体说明"}
                """, taskType, areaName, taskStart, taskEnd, urgency,
                officerTable, scheduledOfficers, areaCaseTypes);
    }

    /**
     * 构建装备推荐 prompt。
     *
     * <p>根据任务类型、时段、区域历史案件分布以及当前空闲装备库存，
     * 推荐执勤必备和可选携带的装备。</p>
     *
     * @param taskType         任务类型
     * @param shiftType        时段（日间/夜间）
     * @param areaName         区域名称
     * @param topCaseTypes     该区域近期多发案件
     * @param availableEquipment 当前空闲装备库存（表格格式）
     * @return AI prompt 字符串
     */
    public static String buildEquipmentPrompt(String taskType, String shiftType, String areaName,
            String topCaseTypes, String availableEquipment) {
        return String.format("""
                你是警务装备推荐助手，请根据任务和区域历史情况推荐携带装备。

                【任务】类型：%s，时段：%s，区域：%s
                【该区域近期多发案件】%s
                【当前空闲装备】（格式：名称 | 类型 | 可用数）
                %s

                以 JSON 回答：
                {"must":[{"name":"名称","reason":"原因"}],"suggested":[{"name":"名称","reason":"原因"}],"summary":"说明"}
                """, taskType, shiftType, areaName, topCaseTypes, availableEquipment);
    }

    /**
     * 构建警情研判 prompt。
     *
     * <p>基于近期警情数据进行综合研判分析，包括趋势分析、高风险区域时段识别及预防建议。</p>
     *
     * @param periodSummary 近30天警情/案件摘要
     * @return AI prompt 字符串
     */
    public static String buildAnalysisPrompt(String periodSummary) {
        return String.format("""
                你是警务数据分析专家，请基于以下近期警情数据进行研判分析。

                【近30天警情/案件摘要】
                %s

                请分析：1. 近期警情规律和趋势 2. 高风险区域和时段 3. 针对性预防建议。
                以 JSON 回答：{"trend":"趋势分析","riskAreas":["区域"],"suggestions":["建议"]}
                """, periodSummary);
    }
}
