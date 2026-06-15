package com.police.alarm.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AlarmQueryDTO {
    private Integer status;
    private String alarmType;
    private Integer urgencyLevel;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 1;
    private Integer size = 20;
    private List<Long> officerAlarmIds;
}
