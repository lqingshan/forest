package com.forest.file.controller;

import com.forest.file.entity.FileCategory;
import com.forest.file.service.FileService;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 文件上传、下载和元数据接口。
 */
@RestController
@RequestMapping({
    ForestApiPaths.CLIENT + "/file",
    ForestApiPaths.ADMIN + "/file",
    ForestApiPaths.PLATFORM + "/file"
})
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload-session")
    public Result<FileService.UploadSessionResult> createUploadSession(@RequestBody CreateUploadSessionRequest request) {
        return Result.success(fileService.createUploadSession(new FileService.CreateUploadSessionCommand(
            request.originalName(),
            request.contentType(),
            request.fileCategory(),
            request.sizeBytes(),
            request.sha256(),
            request.imageWidth(),
            request.imageHeight()
        )));
    }

    @PostMapping("/upload-session/{uploadSessionNo}/complete")
    public Result<FileService.FileInfo> completeUploadSession(@PathVariable String uploadSessionNo) {
        return Result.success(fileService.completeUploadSession(uploadSessionNo));
    }

    @PostMapping("/upload-session/{uploadSessionNo}/abort")
    public Result<FileService.FileInfo> abortUploadSession(@PathVariable String uploadSessionNo) {
        return Result.success(fileService.abortUploadSession(uploadSessionNo));
    }

    @GetMapping("/{fileNo}")
    public Result<FileService.FileInfo> getFile(@PathVariable String fileNo) {
        return Result.success(fileService.getFile(fileNo));
    }

    @PostMapping("/download-url")
    public Result<List<FileService.DownloadUrlResult>> createDownloadUrls(@RequestBody DownloadUrlsRequest request) {
        return Result.success(fileService.createDownloadUrls(request.fileNos()));
    }

    @PostMapping("/preview-url")
    public Result<List<FileService.DownloadUrlResult>> createPreviewUrls(@RequestBody DownloadUrlsRequest request) {
        return Result.success(fileService.createPreviewUrls(request.fileNos()));
    }

    @GetMapping("/{fileNo}/download")
    public ResponseEntity<Void> download(@PathVariable String fileNo) {
        FileService.DownloadUrlResult result = fileService.createDownloadUrl(fileNo);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(result.url()))
            .build();
    }

    @DeleteMapping("/{fileNo}")
    public Result<Void> deleteFile(@PathVariable String fileNo) {
        fileService.deleteFile(fileNo);
        return Result.success(null);
    }

    public record CreateUploadSessionRequest(
        String originalName,
        String contentType,
        FileCategory fileCategory,
        long sizeBytes,
        String sha256,
        Integer imageWidth,
        Integer imageHeight
    ) {
    }

    public record DownloadUrlsRequest(List<String> fileNos) {
    }
}
