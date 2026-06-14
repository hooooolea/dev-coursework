package com.police.caseinfo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CaseQueryDTO {
    private String keyword;         // 案件名称/编号
    private String caseCategory;
    private String caseType;
    private String status;
    private Integer severityLevel;
    private Long leadOfficerId;
    private Long deptId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 1;
    private Integer size = 20;
}
