package com.police.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 文件存储配置
 *
 * <p>配置项前缀 {@code police.file}，支持本地存储；如未来迁移 OSS/OBS，
 * 仅需替换 {@code FileStorageService} 实现，配置层无需改动。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "police.file")
public class FileStorageConfig {

    /** 本地存储根目录（绝对路径） */
    private String uploadPath = "/opt/police/uploads/";

    /** 对外访问前缀（前端拼 URL 用），后端对应 /api/file/download */
    private String accessPrefix = "/api/file/download/";

    /** 案件证据允许的文件扩展名（小写） */
    private List<String> evidenceAllowTypes = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "zip", "rar", "7z",
            "mp4", "mov", "avi", "mp3", "wav"
    );

    /** 案件证据单文件大小上限（字节），默认 10MB */
    private long evidenceMaxSize = 10L * 1024 * 1024;

    /** 头像允许的文件扩展名（小写） */
    private List<String> avatarAllowTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /** 头像单文件大小上限（字节），默认 2MB */
    private long avatarMaxSize = 2L * 1024 * 1024;

    /**
     * 解析存储根目录（去掉末尾斜杠，统一格式）
     */
    public String resolveRootPath() {
        String p = uploadPath == null ? "" : uploadPath.trim();
        if (p.endsWith("/") || p.endsWith("\\")) {
            return p.substring(0, p.length() - 1);
        }
        return p;
    }

    /**
     * 根据相对路径拼出对外可访问的 URL
     *
     * @param relativePath 相对 uploadPath 的子路径，如 {@code case/1/abc.jpg}
     */
    public String resolveAccessUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return null;
        String rp = relativePath.replace("\\", "/");
        if (rp.startsWith("/")) rp = rp.substring(1);
        return accessPrefix + rp;
    }

    /**
     * 根据相对路径拼出磁盘绝对路径
     */
    public String resolveAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return null;
        String rp = relativePath.replace("\\", "/");
        if (rp.startsWith("/")) rp = rp.substring(1);
        return resolveRootPath() + "/" + rp;
    }

    public String extractRelativeFromUrl(String url) {
        if (url == null || !url.startsWith(accessPrefix)) return null;
        return url.substring(accessPrefix.length());
    }
}
