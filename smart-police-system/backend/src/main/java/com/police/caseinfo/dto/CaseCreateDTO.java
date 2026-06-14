package com.police.caseinfo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CaseCreateDTO {

    @NotBlank(message = "案件名称不能为空")
    private String caseName;

    @NotBlank(message = "案件大类不能为空")
    private String caseCategory;

    @NotBlank(message = "案件小类不能为空")
    private String caseType;

    @NotNull(message = "发案时间不能为空")
    private LocalDateTime occurredAt;

    private String locationProvince;
    private String locationCity;
    private String locationDistrict;
    private String locationDetail;

    @NotBlank(message = "案情描述不能为空")
    private String caseDesc;

    private Integer severityLevel = 1;
    private Long leadOfficerId;
    private Long deptId;
    private LocalDate deadlineDate;
    private Long relatedAlarmId;
    private String remark;
}
