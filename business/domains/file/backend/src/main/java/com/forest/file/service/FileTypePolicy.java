package com.forest.file.service;

import com.forest.file.config.ForestFileProperties;
import com.forest.file.entity.FileCategory;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 文件类型、扩展名和大小校验策略。
 */
@Component
public class FileTypePolicy {
    private static final Set<String> IMAGE_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif"
    );

    private static final Set<String> DOCUMENT_TYPES = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/plain"
    );

    private static final Set<String> VIDEO_TYPES = Set.of(
        "video/mp4",
        "video/quicktime",
        "video/x-msvideo"
    );

    private static final Set<String> AUDIO_TYPES = Set.of(
        "audio/mpeg",
        "audio/mp4",
        "audio/aac",
        "audio/wav",
        "audio/x-wav",
        "audio/ogg",
        "audio/flac"
    );

    private static final Map<FileCategory, Set<String>> TYPES = Map.of(
        FileCategory.IMAGE, IMAGE_TYPES,
        FileCategory.DOCUMENT, DOCUMENT_TYPES,
        FileCategory.VIDEO, VIDEO_TYPES,
        FileCategory.AUDIO, AUDIO_TYPES
    );

    private final ForestFileProperties properties;

    public FileTypePolicy(ForestFileProperties properties) {
        this.properties = properties;
    }

    public String normalizeContentType(FileCategory category, String contentType) {
        if (category == null) {
            throw new BusinessException("文件分类不能为空");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new BusinessException("文件 contentType 不能为空");
        }
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        if (!TYPES.getOrDefault(category, Set.of()).contains(normalized)) {
            throw new BusinessException("文件类型不支持：" + contentType);
        }
        return normalized;
    }

    public void validateSize(FileCategory category, long sizeBytes) {
        if (sizeBytes <= 0) {
            throw new BusinessException("文件大小必须大于 0");
        }
        long max = maxSize(category);
        if (sizeBytes > max) {
            throw new BusinessException("文件大小超过限制");
        }
    }

    public long maxSize(FileCategory category) {
        ForestFileProperties.Limits limits = properties.getLimits();
        return switch (category) {
            case IMAGE -> limits.getImageMaxSizeBytes();
            case DOCUMENT -> limits.getDocumentMaxSizeBytes();
            case VIDEO -> limits.getVideoMaxSizeBytes();
            case AUDIO -> limits.getAudioMaxSizeBytes();
        };
    }
}
