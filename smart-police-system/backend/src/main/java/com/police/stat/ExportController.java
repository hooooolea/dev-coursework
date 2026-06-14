package com.police.stat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.alarm.entity.AlarmRecord;
import com.police.alarm.mapper.AlarmRecordMapper;
import com.police.caseinfo.entity.CaseInfo;
import com.police.caseinfo.mapper.CaseInfoMapper;
import com.police.officer.entity.OfficerInfo;
import com.police.officer.mapper.OfficerInfoMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor
public class ExportController {

    private final AlarmRecordMapper alarmMapper;
    private final CaseInfoMapper caseMapper;
    private final OfficerInfoMapper officerMapper;

    /**
     * 导出报表
     * reportType: alarm | case | officer
     */
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public void export(
            @RequestParam String reportType,
            HttpServletResponse response) throws IOException {

        String fileName;
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            switch (reportType) {
                case "alarm"   -> { fileName = buildAlarmReport(wb);   }
                case "case"    -> { fileName = buildCaseReport(wb);    }
                case "officer" -> { fileName = buildOfficerReport(wb); }
                default -> throw new IllegalArgumentException("Unknown reportType: " + reportType);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            wb.write(response.getOutputStream());
        }
    }

    /* =================== 警情汇报 =================== */
    private String buildAlarmReport(XSSFWorkbook wb) {
        Sheet sheet = wb.createSheet("警情汇报");
        CellStyle titleStyle = makeTitleStyle(wb);
        CellStyle headerStyle = makeHeaderStyle(wb);

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月"));
        Row title = sheet.createRow(0);
        Cell tc = title.createCell(0);
        tc.setCellValue(month + " 警情汇报");
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        String[] headers = {"警情编号", "报警时间", "警情类型", "紧急程度", "地址", "处置状态", "结案摘要"};
        Row hRow = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        List<AlarmRecord> list = alarmMapper.selectList(
                new LambdaQueryWrapper<AlarmRecord>()
                        .eq(AlarmRecord::getIsDeleted, 0)
                        .ge(AlarmRecord::getAlarmTime, monthStart.atStartOfDay())
                        .orderByDesc(AlarmRecord::getAlarmTime));

        String[] urgency = {"", "一般", "较紧急", "紧急", "特急"};
        String[] status  = {"", "待处置", "处置中", "已处置", "已关闭"};
        int rowNum = 2;
        for (AlarmRecord r : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(nvl(r.getAlarmNo()));
            row.createCell(1).setCellValue(r.getAlarmTime() != null ? r.getAlarmTime().toString().replace("T", " ") : "");
            row.createCell(2).setCellValue(nvl(r.getAlarmType()));
            row.createCell(3).setCellValue(r.getUrgencyLevel() != null && r.getUrgencyLevel() < urgency.length ? urgency[r.getUrgencyLevel()] : "");
            row.createCell(4).setCellValue(nvl(r.getLocationDetail()));
            row.createCell(5).setCellValue(r.getStatus() != null && r.getStatus() < status.length ? status[r.getStatus()] : "");
            row.createCell(6).setCellValue(nvl(r.getCloseSummary()));
        }
        autoWidth(sheet, headers.length);
        return month + "警情汇报.xlsx";
    }

    /* =================== 案件统计 =================== */
    private String buildCaseReport(XSSFWorkbook wb) {
        Sheet sheet = wb.createSheet("案件统计");
        CellStyle titleStyle = makeTitleStyle(wb);
        CellStyle headerStyle = makeHeaderStyle(wb);

        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月"));
        Row title = sheet.createRow(0);
        Cell tc = title.createCell(0);
        tc.setCellValue(period + " 案件情况统计表");
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        String[] headers = {"案件编号", "案件名称", "案件类型", "紧急程度", "立案日期", "负责警员", "当前状态", "结案日期"};
        Row hRow = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        List<CaseInfo> list = caseMapper.selectList(
                new LambdaQueryWrapper<CaseInfo>()
                        .eq(CaseInfo::getIsDeleted, 0)
                        .orderByDesc(CaseInfo::getFileDate));

        Map<String, String> statusLabel = Map.of(
                "pending", "待立案", "investigating", "侦查中",
                "transferred", "已移送", "closed", "已结案", "cancelled", "已撤案");
        Map<Integer, String> severity = Map.of(1, "一般", 2, "重要", 3, "重大", 4, "特重大");

        int rowNum = 2;
        for (CaseInfo c : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(nvl(c.getCaseNo()));
            row.createCell(1).setCellValue(nvl(c.getCaseName()));
            row.createCell(2).setCellValue(nvl(c.getCaseType()));
            row.createCell(3).setCellValue(c.getSeverityLevel() != null ? severity.getOrDefault(c.getSeverityLevel(), "") : "");
            row.createCell(4).setCellValue(c.getFileDate() != null ? c.getFileDate().toString() : "");
            row.createCell(5).setCellValue(c.getLeadOfficerId() != null ? c.getLeadOfficerId().toString() : "");
            row.createCell(6).setCellValue(c.getStatus() != null ? statusLabel.getOrDefault(c.getStatus(), c.getStatus()) : "");
            row.createCell(7).setCellValue(c.getClosedDate() != null ? c.getClosedDate().toString() : "");
        }
        autoWidth(sheet, headers.length);
        return period + "案件统计表.xlsx";
    }

    /* =================== 警员考勤 =================== */
    private String buildOfficerReport(XSSFWorkbook wb) {
        Sheet sheet = wb.createSheet("警员信息");
        CellStyle titleStyle = makeTitleStyle(wb);
        CellStyle headerStyle = makeHeaderStyle(wb);

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月"));
        Row title = sheet.createRow(0);
        Cell tc = title.createCell(0);
        tc.setCellValue(month + " 警员信息汇总");
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        String[] headers = {"警号", "姓名", "警察类别", "警衔", "当前状态", "入职日期"};
        Row hRow = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        List<OfficerInfo> list = officerMapper.selectList(
                new LambdaQueryWrapper<OfficerInfo>()
                        .eq(OfficerInfo::getIsDeleted, 0)
                        .orderByAsc(OfficerInfo::getBadgeNo));

        Map<String, String> statusLabel = Map.of(
                "on_duty", "在岗", "vacation", "休假", "business", "外出公干", "suspended", "停职");

        int rowNum = 2;
        for (OfficerInfo o : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(nvl(o.getBadgeNo()));
            row.createCell(1).setCellValue(nvl(o.getRealName()));
            row.createCell(2).setCellValue(nvl(o.getPoliceCategory()));
            row.createCell(3).setCellValue(nvl(o.getRankTitle()));
            row.createCell(4).setCellValue(o.getWorkStatus() != null ? statusLabel.getOrDefault(o.getWorkStatus(), o.getWorkStatus()) : "");
            row.createCell(5).setCellValue(o.getEntryDate() != null ? o.getEntryDate().toString() : "");
        }
        autoWidth(sheet, headers.length);
        return month + "警员信息汇总.xlsx";
    }

    /* =================== 样式工具 =================== */
    private CellStyle makeTitleStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle makeHeaderStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private void autoWidth(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 512, 15360));
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
