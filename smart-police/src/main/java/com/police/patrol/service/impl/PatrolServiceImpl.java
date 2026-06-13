package com.police.patrol.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.common.exception.BusinessException;
import com.police.common.util.SecurityUtil;
import com.police.patrol.entity.PatrolCheckin;
import com.police.patrol.entity.PatrolTask;
import com.police.patrol.mapper.PatrolCheckinMapper;
import com.police.patrol.mapper.PatrolTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PatrolServiceImpl extends ServiceImpl<PatrolTaskMapper, PatrolTask> {

    private final PatrolCheckinMapper checkinMapper;
    private final AtomicInteger seq = new AtomicInteger(0);

    public IPage<PatrolTask> listPage(int page, int size, Long officerId, String status) {
        LambdaQueryWrapper<PatrolTask> wrapper = new LambdaQueryWrapper<PatrolTask>()
                .orderByDesc(PatrolTask::getTaskStart);
        if (officerId != null) wrapper.eq(PatrolTask::getOfficerId, officerId);
        if (status != null && !status.isBlank()) wrapper.eq(PatrolTask::getStatus, status);
        return this.page(new Page<>(page, size), wrapper);
    }

    public Long createTask(PatrolTask task) {
        task.setTaskNo("XL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%03d", seq.incrementAndGet() % 1000));
        task.setStatus("pending");
        task.setDispatchBy(SecurityUtil.getCurrentUserId());
        save(task);
        return task.getId();
    }

    public void acceptTask(Long id) {
        PatrolTask task = getById(id);
        if (task == null) throw BusinessException.of("任务不存在");
        task.setStatus("accepted");
        task.setAcceptTime(LocalDateTime.now());
        updateById(task);
    }

    public void checkin(Long taskId, String type, String note) {
        PatrolTask task = getById(taskId);
        if (task == null) throw BusinessException.of("任务不存在");

        PatrolCheckin ci = new PatrolCheckin();
        ci.setTaskId(taskId);
        ci.setOfficerId(task.getOfficerId());
        ci.setCheckinType(type);
        ci.setCheckinTime(LocalDateTime.now());
        ci.setNote(note);
        checkinMapper.insert(ci);

        if ("start".equals(type)) {
            task.setStatus("ongoing");
            updateById(task);
        } else if ("end".equals(type)) {
            task.setStatus("completed");
            task.setCompleteTime(LocalDateTime.now());
            updateById(task);
        }
    }

    public void completeTask(Long id, String summary) {
        PatrolTask task = getById(id);
        if (task == null) throw BusinessException.of("任务不存在");
        task.setStatus("completed");
        task.setCompleteTime(LocalDateTime.now());
        task.setSummary(summary);
        updateById(task);
    }

    public List<PatrolCheckin> listCheckins(Long taskId) {
        return checkinMapper.selectList(new LambdaQueryWrapper<PatrolCheckin>()
                .eq(PatrolCheckin::getTaskId, taskId)
                .orderByAsc(PatrolCheckin::getCheckinTime));
    }
}
