package com.police.alarm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmCreateDTO {

    @NotNull(message = "报警时间不能为空")
    private LocalDateTime alarmTime;

    private String callerName;

    @NotBlank(message = "联系电话不能为空")
    private String callerPhone;

    private String locationProvince;
    private String locationCity;
    private String locationDistrict;
    private String locationDetail;

    @NotBlank(message = "警情类型不能为空")
    private String alarmType;

    @NotBlank(message = "警情描述不能为空")
    private String alarmDesc;

    private Integer urgencyLevel = 2;
}
