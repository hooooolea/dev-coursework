package com.police.alarm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.alarm.dto.AlarmCreateDTO;
import com.police.alarm.dto.AlarmQueryDTO;
import com.police.alarm.entity.AlarmRecord;

public interface AlarmService extends IService<AlarmRecord> {
    /** 接警录入，返回警情ID */
    Long create(AlarmCreateDTO dto);
    /** 分页查询 */
    IPage<AlarmRecord> listPage(AlarmQueryDTO query);
    /** 派发任务 */
    void dispatch(Long alarmId, Long officerId);
    /** 到达现场 */
    void arrive(Long dispatchId);
    /** 关闭警情 */
    void close(Long alarmId, String summary);
}
