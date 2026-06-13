package com.police.common.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.police.common.config.FileStorageConfig;
import com.police.common.exception.BusinessException;
import com.police.common.result.Result;
import com.police.common.util.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用文件访问接口
 *
 * <p>前端通过 {@code FileStorageConfig.accessPrefix}（默认 {@code /api/file/download/}）
 * 拼出访问 URL 即可下载/预览已上传的证据、头像等文件。路径中包含 {@code ..}
 * 一律拒绝，防止越权访问 upload-path 之外的文件。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageConfig config;
    private final FileStorageService fileStorageService;

    /**
     * 上传文件到业务子目录
     *
     * @param biz 业务类型：evidence（证据材料）/ avatar（用户头像）/ misc（其他），默认 misc
     * @return 文件元数据（originalName, relativePath, accessUrl, ext, size, contentType）
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "biz", defaultValue = "misc") String biz) {

        List<String> allowExts;
        long maxSize;

        switch (biz) {
            case "avatar":
                allowExts = config.getAvatarAllowTypes();
                maxSize = config.getAvatarMaxSize();
                break;
            case "evidence":
                allowExts = config.getEvidenceAllowTypes();
                maxSize = config.getEvidenceMaxSize();
                break;
            default: // misc
                allowExts = config.getEvidenceAllowTypes();
                maxSize = 20L * 1024 * 1024;
                break;
        }

        FileStorageService.StoredFile stored = fileStorageService.store(file, biz, allowExts, maxSize);

        Map<String, Object> data = new HashMap<>();
        data.put("originalName", stored.getOriginalName());
        data.put("relativePath", stored.getRelativePath());
        data.put("accessUrl", stored.getAccessUrl());
        data.put("ext", stored.getExt());
        data.put("size", stored.getSize());
        data.put("contentType", stored.getContentType());

        return Result.ok(data);
    }

    /**
     * 下载/预览文件
     *
     * <p>URL 形态：{@code /api/file/download/<biz>/yyyy/MM/xxx.ext}，
     * 完整相对路径从 URL 中提取（去掉 accessPrefix 前缀）。</p>
     *
     * @param inline true=内联预览（图片等），false=附件下载，默认 true
     */
    @GetMapping("/download/**")
    public void download(jakarta.servlet.http.HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam(name = "inline", defaultValue = "true") boolean inline) throws IOException {
        String fullPath = extractRelativePath(request);
        if (fullPath == null || fullPath.isEmpty()) {
            throw new BusinessException("文件路径为空");
        }
        if (fullPath.contains("..")) {
            throw new BusinessException("非法文件路径");
        }
        String absolutePath = config.resolveAbsolutePath(fullPath);
        File file = FileUtil.file(absolutePath);
        if (!file.exists() || !file.isFile()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = guessContentType(file.getName());
        response.setContentType(contentType);
        response.setContentLengthLong(file.length());

        String dispositionType = inline ? "inline" : "attachment";
        String encoded = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                dispositionType + "; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);

        try (OutputStream os = response.getOutputStream()) {
            IoUtil.copy(FileUtil.getInputStream(file), os);
        }
    }

    /** 预览图片等小文件的便捷接口（默认 inline=true） */
    @GetMapping("/preview/**")
    public ResponseEntity<Resource> preview(jakarta.servlet.http.HttpServletRequest request) {
        String fullPath = extractRelativePath(request);
        if (fullPath == null || fullPath.contains("..")) {
            throw new BusinessException("非法文件路径");
        }
        File file = FileUtil.file(config.resolveAbsolutePath(fullPath));
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(guessContentType(file.getName())));
        return ResponseEntity.ok().headers(headers).body(new FileSystemResource(file));
    }

    private String extractRelativePath(jakarta.servlet.http.HttpServletRequest request) {
        String prefix = config.getAccessPrefix();
        String uri = request.getRequestURI();
        if (!uri.startsWith(prefix)) return null;
        String rest = uri.substring(prefix.length());
        // 去掉可能的查询串
        int q = rest.indexOf('?');
        if (q >= 0) rest = rest.substring(0, q);
        return rest;
    }

    private String guessContentType(String filename) {
        String ext = com.police.common.util.FileStorageService.extOf(filename);
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png"         -> "image/png";
            case "gif"         -> "image/gif";
            case "webp"        -> "image/webp";
            case "bmp"         -> "image/bmp";
            case "pdf"         -> "application/pdf";
            case "mp4"         -> "video/mp4";
            case "mov"         -> "video/quicktime";
            case "mp3"         -> "audio/mpeg";
            case "wav"         -> "audio/wav";
            case "zip"         -> "application/zip";
            case "doc"         -> "application/msword";
            case "docx"        -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls"         -> "application/vnd.ms-excel";
            case "xlsx"        -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt"         -> "application/vnd.ms-powerpoint";
            case "pptx"        -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt"         -> "text/plain;charset=UTF-8";
            default            -> "application/octet-stream";
        };
    }
}
