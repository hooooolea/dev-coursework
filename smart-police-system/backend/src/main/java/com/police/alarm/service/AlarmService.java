package com.police.alarm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.alarm.dto.AlarmCreateDTO;
import com.police.alarm.dto.AlarmQueryDTO;
import com.police.alarm.entity.AlarmRecord;

public interface AlarmService extends IService<AlarmRecord> {
    Long create(AlarmCreateDTO dto);
    IPage<AlarmRecord> listPage(AlarmQueryDTO query);
    /** 查询派发给当前登录警员的警情 */
    IPage<AlarmRecord> listMyTasks(int page, int size);
    void dispatch(Long alarmId, Long officerId);
    void arrive(Long dispatchId);
    void arriveByAlarm(Long alarmId);
    void close(Long alarmId, String summary);
}
