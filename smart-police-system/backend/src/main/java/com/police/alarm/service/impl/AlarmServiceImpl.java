package com.police.alarm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.ai.service.AiEquipmentService;
import com.police.alarm.dto.AlarmCreateDTO;
import com.police.alarm.dto.AlarmQueryDTO;
import com.police.alarm.entity.AlarmDispatch;
import com.police.alarm.entity.AlarmRecord;
import com.police.alarm.mapper.AlarmDispatchMapper;
import com.police.alarm.mapper.AlarmRecordMapper;
import com.police.alarm.service.AlarmService;
import com.police.common.exception.BusinessException;
import com.police.common.util.SecurityUtil;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.mapper.OfficerInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AlarmServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord> implements AlarmService {

    private final AlarmDispatchMapper dispatchMapper;
    private final OfficerInfoMapper officerMapper;
    private final AiEquipmentService aiEquipmentService;
    private final AtomicInteger seqCounter = new AtomicInteger(0);

    @Override
    public Long create(AlarmCreateDTO dto) {
        AlarmRecord alarm = new AlarmRecord();
        alarm.setAlarmNo(generateAlarmNo());
        alarm.setAlarmTime(dto.getAlarmTime());
        alarm.setCallerName(dto.getCallerName());
        alarm.setCallerPhone(dto.getCallerPhone());
        alarm.setLocationProvince(dto.getLocationProvince());
        alarm.setLocationCity(dto.getLocationCity());
        alarm.setLocationDistrict(dto.getLocationDistrict());
        alarm.setLocationDetail(dto.getLocationDetail());
        alarm.setAlarmType(dto.getAlarmType());
        alarm.setAlarmDesc(dto.getAlarmDesc());
        alarm.setUrgencyLevel(dto.getUrgencyLevel());
        alarm.setStatus(1);
        alarm.setDutyUserId(SecurityUtil.getCurrentUserId());
        save(alarm);
        return alarm.getId();
    }

    @Override
    public IPage<AlarmRecord> listPage(AlarmQueryDTO query) {
        Page<AlarmRecord> page = new Page<>(query.getPage(), query.getSize());
        return baseMapper.selectPage(page, query);
    }

    @Override
    public IPage<AlarmRecord> listMyTasks(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<AlarmDispatch> dispatches = dispatchMapper.selectList(
            new LambdaQueryWrapper<AlarmDispatch>()
                .eq(AlarmDispatch::getOfficerId, userId)
                .in(AlarmDispatch::getStatus, 1, 2));
        if (dispatches.isEmpty()) {
            IPage<AlarmRecord> empty = new Page<>(page, size);
            empty.setTotal(0);
            return empty;
        }
        List<Long> alarmIds = dispatches.stream().map(AlarmDispatch::getAlarmId).collect(java.util.stream.Collectors.toList());
        LambdaQueryWrapper<AlarmRecord> q = new LambdaQueryWrapper<AlarmRecord>()
                .in(AlarmRecord::getId, alarmIds)
                .orderByDesc(AlarmRecord::getAlarmTime);
        return baseMapper.selectPage(new Page<>(page, size), q);
    }

    public void arriveByAlarm(Long alarmId) {
        List<AlarmDispatch> list = dispatchMapper.selectList(
            new LambdaQueryWrapper<AlarmDispatch>().eq(AlarmDispatch::getAlarmId, alarmId));
        if (list.isEmpty()) throw BusinessException.of("该警情未派发");
        arrive(list.get(0).getId());
    }

    @Override
    public void dispatch(Long alarmId, Long officerId) {
        // 1. 校验警情
        AlarmRecord alarm = getById(alarmId);
        if (alarm == null) throw BusinessException.of("警情不存在");
        if (alarm.getStatus() == 4) throw BusinessException.of("警情已关闭，无法派发");

        // 2. 校验警员在岗
        OfficerInfo officer = officerMapper.selectById(officerId);
        if (officer == null) throw BusinessException.of("警员不存在");
        if (!"on_duty".equals(officer.getWorkStatus())) {
            throw BusinessException.of("警员" + officer.getRealName() + "当前不在岗，状态：" + officer.getWorkStatus());
        }

        // 3. 写派发记录
        AlarmDispatch dispatch = new AlarmDispatch();
        dispatch.setAlarmId(alarmId);
        dispatch.setOfficerId(officerId);
        dispatch.setDispatchTime(LocalDateTime.now());
        dispatch.setStatus(1);
        dispatchMapper.insert(dispatch);

        // 4. 更新警情状态
        alarm.setStatus(2);
        updateById(alarm);

        // 5. 前端自行触发 AI 装备推荐（避免阻塞派发响应）
    }

    @Override
    public void arrive(Long dispatchId) {
        AlarmDispatch dispatch = dispatchMapper.selectById(dispatchId);
        if (dispatch == null) throw BusinessException.of("派发记录不存在");
        dispatch.setArriveTime(LocalDateTime.now());
        dispatch.setStatus(3);
        dispatchMapper.updateById(dispatch);
    }

    @Override
    public void close(Long alarmId, String summary) {
        AlarmRecord alarm = getById(alarmId);
        if (alarm == null) throw BusinessException.of("警情不存在");
        alarm.setStatus(4);
        alarm.setCloseTime(LocalDateTime.now());
        alarm.setCloseSummary(summary);
        updateById(alarm);
    }

    private String generateAlarmNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 从数据库中查当天最大序号，避免重复
        int maxSeq = 0;
        List<AlarmRecord> todayList = baseMapper.selectList(
            new LambdaQueryWrapper<AlarmRecord>()
                .likeRight(AlarmRecord::getAlarmNo, "BJ" + date)
                .orderByDesc(AlarmRecord::getAlarmNo)
                .last("LIMIT 1"));
        if (!todayList.isEmpty()) {
            String lastNo = todayList.get(0).getAlarmNo();
            try { maxSeq = Integer.parseInt(lastNo.substring(lastNo.length() - 3)); } catch (Exception ignored) {}
        }
        int seq = Math.max(seqCounter.incrementAndGet(), maxSeq + 1) % 1000;
        return "BJ" + date + String.format("%03d", seq);
    }
}
