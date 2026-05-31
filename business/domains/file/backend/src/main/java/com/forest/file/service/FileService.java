package com.forest.file.service;

import com.forest.file.entity.FileCategory;
import com.forest.file.entity.FileStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件业务服务。
 *
 * <p>文件域只负责“文件本身”的生命周期：创建上传会话、记录文件元数据、确认 OSS 直传结果、
 * 生成私有下载 URL、删除文件元数据和对象存储文件。具体业务模块只保存 {@code fileNo}，
 * 不直接保存 OSS 永久地址，也不直接拼 objectKey。</p>
 *
 * <p>一期上传采用“前端直传 OSS + 后端 complete 确认”的模式。一次文件上传不是单次 HTTP 请求，
 * 而是下面这条状态链路：</p>
 *
 * <pre>
 * createUploadSession
 *   -> file_object = UPLOADING
 *   -> file_upload_session = CREATED
 *   -> 前端拿 credential 直传 OSS
 *   -> completeUploadSession
 *   -> 后端 HEAD OSS 校验大小、类型、etag
 *   -> file_object = AVAILABLE
 * </pre>
 *
 * <p>后端永远以 {@code completeUploadSession} 作为文件可用的分界点：
 * complete 前即使已经生成 {@code fileNo}，业务上也不能把该文件当成可用文件。</p>
 */
public interface FileService {
    /**
     * 创建一次上传会话。
     *
     * <p>调用时机：前端选中文件后、真正上传 OSS 前调用。该方法会立即生成 {@code fileNo}
     * 和 {@code uploadSessionNo}，写入文件元数据和上传会话，并返回 OSS 直传凭证。</p>
     *
     * <p>职责边界：这里只创建“准备上传”的记录，不代表文件已经上传成功。文件状态会保持为
     * {@link FileStatus#UPLOADING}，直到前端上传 OSS 成功后调用 {@link #completeUploadSession(String)}。</p>
     */
    UploadSessionResult createUploadSession(CreateUploadSessionCommand command);

    /**
     * 完成上传会话。
     *
     * <p>调用时机：前端使用直传凭证把文件上传到 OSS 成功后调用。后端会通过对象存储 HEAD
     * 查询真实对象，并校验对象是否存在、大小是否匹配、contentType 是否匹配。</p>
     *
     * <p>只有 complete 成功后，文件才会从 {@link FileStatus#UPLOADING} 变为
     * {@link FileStatus#AVAILABLE}，业务模块才能正式保存或使用这个 {@code fileNo}。</p>
     */
    FileInfo completeUploadSession(String uploadSessionNo);

    /**
     * 中止上传会话。
     *
     * <p>调用时机：前端取消上传、页面关闭前主动放弃、或业务明确不再需要本次上传时调用。
     * 中止后对应文件不会变成可用文件。</p>
     */
    FileInfo abortUploadSession(String uploadSessionNo);

    /**
     * 查询文件元数据。
     *
     * <p>该方法会做当前登录用户的访问校验。默认策略是一期开启“上传者本人可读”，后续可通过
     * {@link FileAccessPolicy} 扩展为平台、商家、组织或 RBAC 权限。</p>
     */
    FileInfo getFile(String fileNo);

    /**
     * 批量创建私有下载 URL。
     *
     * <p>用于列表页、详情页一次性换取多个文件的短期签名下载地址。返回的 URL 有过期时间，
     * 前端不能长期保存；业务库也不能保存该 URL。</p>
     */
    List<DownloadUrlResult> createDownloadUrls(List<String> fileNos);

    /**
     * 批量创建私有预览 URL。
     *
     * <p>预览 URL 与下载 URL 一样都是短期签名地址，区别在于响应头倾向于
     * {@code inline}，用于图片、视频、音频和 PDF 等浏览器/小程序可直接展示的场景。
     * 前端仍不能长期保存该 URL。</p>
     */
    List<DownloadUrlResult> createPreviewUrls(List<String> fileNos);

    /**
     * 创建单个私有下载 URL。
     *
     * <p>适合预览、点击下载、后端重定向下载等单文件场景。下载前会校验文件状态和当前用户权限。</p>
     */
    DownloadUrlResult createDownloadUrl(String fileNo);

    /**
     * 创建单个私有预览 URL。
     *
     * <p>用于头像、图片预览、音视频播放和 PDF 预览。是否真正以内联方式展示，最终由客户端能力和
     * OSS 响应头共同决定。</p>
     */
    DownloadUrlResult createPreviewUrl(String fileNo);

    /**
     * 删除文件。
     *
     * <p>当前实现会校验当前用户是否有删除权限，然后删除对象存储文件并把文件元数据标记为删除。
     * 删除后该 {@code fileNo} 不再可下载。</p>
     */
    void deleteFile(String fileNo);

    /**
     * 创建上传会话的入参。
     *
     * @param originalName 用户选择文件时的原始文件名，用于展示和生成安全文件名
     * @param contentType 前端识别到的 MIME 类型，后端会结合 {@code fileCategory} 做基础校验
     * @param fileCategory 文件分类，当前支持图片、文档、小视频、音频
     * @param sizeBytes 文件大小，后端按分类限制大小：图片较小，文档和视频一期最多 50MB
     * @param sha256 文件摘要，一期允许为空，后续可用于秒传、去重或完整性校验
     * @param imageWidth 图片宽度；仅图片有意义，文档、视频和音频传 {@code null}
     * @param imageHeight 图片高度；仅图片有意义，文档、视频和音频传 {@code null}
     */
    record CreateUploadSessionCommand(
        String originalName,
        String contentType,
        FileCategory fileCategory,
        long sizeBytes,
        String sha256,
        Integer imageWidth,
        Integer imageHeight
    ) {
    }

    /**
     * OSS 直传凭证。
     *
     * <p>前端拿到该对象后，直接向 {@code uploadUrl} 上传文件。对于阿里云 OSS POST 直传，
     * {@code formFields} 是必须随表单提交的签名字段；{@code headers} 预留给其他上传方式或供应商。</p>
     */
    record UploadCredentialVO(
        String uploadUrl,
        String method,
        Map<String, String> formFields,
        Map<String, String> headers,
        LocalDateTime expiresAt
    ) {
    }

    /**
     * 创建上传会话的返回值。
     *
     * <p>{@code fileNo} 会在创建会话时立即生成，但此时文件仍是 {@link FileStatus#UPLOADING}。
     * 前端必须先使用 {@code credential} 完成 OSS 直传，再调用 complete，文件才真正可用。</p>
     */
    record UploadSessionResult(
        String uploadSessionNo,
        String fileNo,
        String bucket,
        String objectKey,
        UploadCredentialVO credential,
        FileInfo file
    ) {
    }

    /**
     * 私有下载 URL 返回值。
     *
     * <p>{@code url} 是短期签名地址，只能作为临时下载或预览入口，不能作为业务长期字段保存。</p>
     */
    record DownloadUrlResult(
        String fileNo,
        String url,
        LocalDateTime expiresAt
    ) {
    }

    /**
     * 文件元数据视图。
     *
     * <p>这是业务模块和前端识别文件的稳定摘要。业务表应保存 {@code fileNo}，
     * 需要展示或下载时再通过文件域查询元数据或换取签名 URL。</p>
     */
    record FileInfo(
        String fileNo,
        String businessAppCode,
        String uploadedClientAppCode,
        String originalName,
        String contentType,
        FileCategory fileCategory,
        long sizeBytes,
        String etag,
        FileStatus status,
        LocalDateTime createdTime
    ) {
    }
}
