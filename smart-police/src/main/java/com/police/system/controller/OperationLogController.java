package com.police.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.common.result.Result;
import com.police.system.entity.SysOperationLog;
import com.police.system.mapper.SysOperationLogMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final SysOperationLogMapper logMapper;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:log:view')")
    public Result<IPage<SysOperationLog>> list(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        LambdaQueryWrapper<SysOperationLog> q = new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getCreatedAt);

        if (module != null && !module.isBlank())
            q.eq(SysOperationLog::getModule, module);
        if (userName != null && !userName.isBlank())
            q.like(SysOperationLog::getUserName, userName);
        if (startTime != null && !startTime.isBlank())
            q.ge(SysOperationLog::getCreatedAt, LocalDateTime.parse(startTime.replace(" ", "T")));
        if (endTime != null && !endTime.isBlank())
            q.le(SysOperationLog::getCreatedAt, LocalDateTime.parse(endTime.replace(" ", "T")));

        return Result.ok(logMapper.selectPage(new Page<>(page, size), q));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('sys:log:view')")
    public void export(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletResponse response) throws IOException {

        LambdaQueryWrapper<SysOperationLog> q = new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getCreatedAt);

        if (module != null && !module.isBlank())
            q.eq(SysOperationLog::getModule, module);
        if (userName != null && !userName.isBlank())
            q.like(SysOperationLog::getUserName, userName);
        if (startTime != null && !startTime.isBlank())
            q.ge(SysOperationLog::getCreatedAt, LocalDateTime.parse(startTime.replace(" ", "T")));
        if (endTime != null && !endTime.isBlank())
            q.le(SysOperationLog::getCreatedAt, LocalDateTime.parse(endTime.replace(" ", "T")));

        // 最多导出 10000 条
        List<SysOperationLog> logs = logMapper.selectList(q.last("LIMIT 10000"));

        String filename = "操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20") + "\"");

        String[] headers = {"ID", "操作人", "操作模块", "操作动作", "请求参数", "IP地址", "执行耗时(ms)", "操作时间"};

        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet("操作日志");

            // 表头样式
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 数据样式
            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            for (int i = 0; i < logs.size(); i++) {
                Row row = sheet.createRow(i + 1);
                SysOperationLog log = logs.get(i);

                row.createCell(0).setCellValue(log.getId() != null ? log.getId().doubleValue() : 0);
                row.createCell(1).setCellValue(log.getUserName() != null ? log.getUserName() : "");
                row.createCell(2).setCellValue(log.getModule() != null ? log.getModule() : "");
                row.createCell(3).setCellValue(log.getAction() != null ? log.getAction() : "");
                row.createCell(4).setCellValue(log.getRequestBody() != null ? log.getRequestBody() : "");
                row.createCell(5).setCellValue(log.getRequestIp() != null ? log.getRequestIp() : "");
                row.createCell(6).setCellValue(log.getExecuteTime() != null ? log.getExecuteTime().doubleValue() : 0);
                row.createCell(7).setCellValue(log.getCreatedAt() != null ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");

                for (int j = 0; j < 8; j++) {
                    row.getCell(j).setCellStyle(dataStyle);
                }
            }

            // 自适应列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(response.getOutputStream());
            wb.dispose(); // 释放 SXSSF 临时文件
        }
    }
}
