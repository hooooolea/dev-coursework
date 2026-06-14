package com.police.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.ai.client.MiMoClient;
import com.police.ai.util.PromptBuilder;
import com.police.equipment.entity.EquipmentInfo;
import com.police.equipment.mapper.EquipmentInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * AI 装备推荐 服务
 *
 * <p>根据任务类型、时段、区域及区域历史案件分布，结合当前可用装备库存，
 * 调用 MiMo 大模型推荐执勤所需携带的装备。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiEquipmentService {

    private final EquipmentInfoMapper equipmentMapper;

    private final MiMoClient miMoClient;

    /**
     * 生成装备推荐
     *
     * <p>查询当前未报废的装备列表，构建 prompt 后调用 MiMo Pro 模型
     * 返回推荐的必带和可选装备清单。</p>
     *
     * @param taskType     任务类型（例行巡逻 / 专项检查 / 定点值守）
     * @param shiftType    时段（日间 / 夜间）
     * @param areaName     巡逻区域名称
     * @param topCaseTypes 该区域近期多发案件类型描述
     * @return AI 推荐的装备清单 JSON 文本
     * @throws IOException 网络或 IO 异常
     */
    public String recommend(String taskType, String shiftType, String areaName, String topCaseTypes) throws IOException {
        List<EquipmentInfo> equipment = equipmentMapper.selectList(
                new LambdaQueryWrapper<EquipmentInfo>()
                        .eq(EquipmentInfo::getIsDeleted, 0)
                        .ne(EquipmentInfo::getStatus, "scrapped"));

        StringBuilder equipList = new StringBuilder();
        for (EquipmentInfo e : equipment) {
            equipList.append(String.format("%s | %s | %d\n",
                    e.getEquipName() != null ? e.getEquipName() : "-",
                    e.getEquipType() != null ? e.getEquipType() : "-",
                    1));
        }

        String prompt = PromptBuilder.buildEquipmentPrompt(
                taskType, shiftType, areaName, topCaseTypes, equipList.toString());
        return miMoClient.chatPro("你是警务装备推荐助手。", prompt);
    }
}
