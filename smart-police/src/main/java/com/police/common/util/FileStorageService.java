package com.police.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.police.common.config.FileStorageConfig;
import com.police.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 文件存储服务
 *
 * <p>负责把上传文件落盘到 {@code police.file.upload-path} 下的业务子目录，
 * 返回相对路径与对外访问 URL。所有存储路径相关参数均来自
 * {@link FileStorageConfig}，不在代码中硬编码。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig config;

    /** 日期子目录格式 yyyy/MM */
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy/MM");

    /**
     * 存储上传文件
     *
     * @param file      上传的文件
     * @param biz       业务子目录（evidence / avatar），禁止包含 ..
     * @param allowExts 允许的扩展名（不含点，小写）
     * @param maxSize   最大字节数
     * @return 存储结果（含相对路径、绝对路径、对外访问 URL）
     * @throws BusinessException 校验失败 / 落盘失败
     */
    public StoredFile store(MultipartFile file, String biz, java.util.List<String> allowExts, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        // 1. 大小校验
        if (file.getSize() > maxSize) {
            throw new BusinessException(
                    String.format("文件大小 %.2f MB 超过限制 %.2f MB",
                            file.getSize() / 1024.0 / 1024.0,
                            maxSize / 1024.0 / 1024.0));
        }
        // 2. 扩展名校验
        String original = file.getOriginalFilename();
        String ext = extOf(original);
        if (ext.isEmpty() || !allowExts.contains(ext)) {
            throw new BusinessException("不支持的文件类型: " + (ext.isEmpty() ? "(无扩展名)" : ext));
        }
        // 3. biz 防穿越
        String safeBiz = biz.replace("..", "").replaceAll("[/\\\\]", "");
        if (safeBiz.isEmpty()) safeBiz = "misc";

        // 4. 拼路径：uploadPath/biz/yyyy/MM/<uuid>.<ext>
        String datePart = LocalDate.now().format(DTF);
        String filename = IdUtil.fastSimpleUUID() + "." + ext;
        String relativePath = safeBiz + "/" + datePart + "/" + filename;
        String absolutePath = config.resolveAbsolutePath(relativePath);

        try {
            File parent = FileUtil.file(absolutePath).getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new BusinessException("无法创建存储目录");
            }
            file.transferTo(FileUtil.file(absolutePath));
        } catch (IOException e) {
            log.error("文件落盘失败: {}", absolutePath, e);
            throw new BusinessException("文件保存失败");
        }

        StoredFile result = new StoredFile();
        result.setOriginalName(original);
        result.setRelativePath(relativePath);
        result.setAbsolutePath(absolutePath);
        result.setAccessUrl(config.resolveAccessUrl(relativePath));
        result.setExt(ext);
        result.setSize(file.getSize());
        result.setContentType(file.getContentType());
        return result;
    }

    /** 仅取扩展名（小写，无点）。无扩展名返回空串 */
    public static String extOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 存储结果 DTO */
    @lombok.Data
    public static class StoredFile {
        private String originalName;
        private String relativePath;
        private String absolutePath;
        private String accessUrl;
        private String ext;
        private long size;
        private String contentType;
    }
}
