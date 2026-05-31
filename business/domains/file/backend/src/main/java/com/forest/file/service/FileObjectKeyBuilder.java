package com.forest.file.service;

import com.forest.file.entity.FileCategory;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 生成对象存储 key。
 *
 * <p>对象存储里真正定位一个文件需要两个信息：</p>
 *
 * <pre>
 * bucket + objectKey
 * </pre>
 *
 * <p>{@code FileObjectKeyBuilder} 只负责生成 {@code objectKey}，不负责决定 bucket。
 * bucket 由 {@code ObjectStorageBucketResolver} 根据业务 app 决定，例如：</p>
 *
 * <pre>
 * cxc-commerce -> cxc-commerce-file
 * trade-leads  -> trade-leads
 * </pre>
 *
 * <p>当前约定 local/prod 共用同一个 bucket，所以 objectKey 的第一段必须放环境：</p>
 *
 * <pre>
 * {env}/{category}/{yyyyMMdd}/{fileNo}.{ext}
 * </pre>
 *
 * <p>示例：</p>
 *
 * <pre>
 * local/image/20260511/FILE202605110001.jpg
 * prod/video/20260511/FILE202605110002.mp4
 * prod/document/20260511/FILE202605110003.pdf
 * prod/audio/20260511/FILE202605110004.mp3
 * </pre>
 *
 * <p>为什么这样设计：</p>
 *
 * <ul>
 *   <li>{@code env}：同一个 bucket 下区分 local/prod 数据。</li>
 *   <li>{@code category}：按图片、文档、视频、音频分目录，方便排查和后续生命周期策略。</li>
 *   <li>{@code yyyyMMdd}：避免单目录对象过多，也方便按日期排查，同时减少路径层级。</li>
 *   <li>{@code fileNo.ext}：系统内稳定文件编号作为对象名，避免小程序临时文件名污染 OSS 路径。</li>
 * </ul>
 *
 * <p>注意：objectKey 不使用用户传入的完整文件名，只借用扩展名。用户原始文件名保存在
 * {@code file_object.original_name}，用于前端展示和下载文件名。</p>
 */
@Component
public class FileObjectKeyBuilder {
    /**
     * 生成 OSS objectKey。
     *
     * @param env 文件环境，一期通常是 {@code local} 或 {@code prod}
     * @param category 文件分类，决定 objectKey 第二段目录，例如 {@code image/document/video/audio}
     * @param fileNo 文件模块生成的唯一编号，用于防止同名文件覆盖
     * @param originalName 用户上传时的原始文件名，只用于提取扩展名
     * @return 可直接写入 {@code file_object.object_key} 的对象存储 key
     */
    public String build(String env, FileCategory category, String fileNo, String originalName) {
        String safeEnv = requireText(env, "文件环境不能为空").toLowerCase(Locale.ROOT);
        String safeFileNo = requireText(fileNo, "fileNo 不能为空");
        String fileName = objectFileName(category, safeFileNo, originalName);
        LocalDateTime now = ForestTime.now();
        return String.format(
            "%s/%s/%04d%02d%02d/%s",
            safeEnv,
            category.name().toLowerCase(Locale.ROOT),
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            fileName
        );
    }

    /**
     * 从原始文件名中提取扩展名。
     *
     * <p>扩展名只用于展示、排查或后续策略判断，不能单独作为文件安全校验依据。
     * 真正的上传限制仍然要结合 {@code fileCategory}、{@code contentType} 和文件大小。</p>
     */
    public String extension(String originalName) {
        String fileName = sanitizeFileName(originalName);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String objectFileName(FileCategory category, String fileNo, String originalName) {
        String ext = extension(originalName);
        if (ext == null) {
            ext = defaultExtension(category);
        }
        return ext == null ? fileNo : fileNo + "." + ext;
    }

    private String defaultExtension(FileCategory category) {
        return switch (category) {
            case IMAGE -> "jpg";
            case VIDEO -> "mp4";
            case AUDIO -> "mp3";
            case DOCUMENT -> null;
        };
    }

    /**
     * 清洗用户原始文件名，用于安全提取扩展名。
     *
     * <p>处理规则：</p>
     *
     * <ul>
     *   <li>去掉前端可能传入的本地路径，只保留最后一段文件名。</li>
     *   <li>只允许英文字母、数字、点、下划线和中横线。</li>
     *   <li>其他字符替换为下划线，避免扩展名提取受空格、中文、特殊符号或路径控制字符影响。</li>
     *   <li>拒绝空文件名、{@code .}、{@code ..}。</li>
     *   <li>最长保留 120 个字符，避免 objectKey 过长。</li>
     * </ul>
     */
    private String sanitizeFileName(String originalName) {
        String value = requireText(originalName, "文件名不能为空").trim();
        String normalized = value.replace("\\", "/");
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }
        normalized = normalized.replaceAll("[^A-Za-z0-9._-]", "_");
        if (normalized.isBlank() || ".".equals(normalized) || "..".equals(normalized)) {
            throw new BusinessException("文件名不合法");
        }
        return normalized.length() > 120 ? normalized.substring(normalized.length() - 120) : normalized;
    }

    /**
     * 校验必填字符串。
     */
    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }
}
