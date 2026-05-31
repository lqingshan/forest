package com.forest.file.service;

import com.forest.file.config.FileDomainConfig;
import com.forest.file.entity.FileCategory;
import com.forest.file.entity.FileObjectPO;
import com.forest.file.entity.FileStatus;
import com.forest.file.entity.FileUploadSessionStatus;
import com.forest.file.repository.FileObjectRepository;
import com.forest.file.repository.FileUploadSessionRepository;
import com.forest.file.service.impl.FileServiceImpl;
import com.forest.starter.auth.context.PrincipalContextHolder;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.objectstorage.ObjectStorageBucketResolver;
import com.forest.starter.objectstorage.mock.MockObjectStorageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证文件直传主流程。
 */
@SpringBootTest(classes = FileServiceIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
class FileServiceIntegrationTest {
    @Autowired
    private FileService fileService;

    @Autowired
    private FileObjectRepository fileObjectRepository;

    @Autowired
    private FileUploadSessionRepository uploadSessionRepository;

    @Autowired
    private MockObjectStorageClient objectStorageClient;

    @BeforeEach
    void setUp() {
        uploadSessionRepository.deleteAll();
        fileObjectRepository.deleteAll();
        setAuth(1001L);
    }

    @AfterEach
    void tearDown() {
        PrincipalContextHolder.clear();
    }

    @Test
    void createUploadSessionCreatesFileNoAndUploadingRowsBeforeComplete() {
        FileService.UploadSessionResult result = createImageUploadSession(2048);

        assertThat(result.fileNo()).startsWith("FILE");
        assertThat(result.uploadSessionNo()).startsWith("FUS");
        assertThat(result.bucket()).isEqualTo("cxc-commerce-file");
        assertThat(result.objectKey()).matches("local/image/\\d{8}/" + result.fileNo() + "\\.jpg");
        assertThat(result.credential().uploadUrl()).isEqualTo("https://mock-oss.local/cxc-commerce-file");

        FileObjectPO file = fileObjectRepository.findByFileNoAndDeleted(result.fileNo(), 0).orElseThrow();
        assertThat(file.getStatus()).isEqualTo(FileStatus.UPLOADING);
        assertThat(file.getUploadedClientAppCode()).isEqualTo("cxc-commerce-buyer-wechat-miniapp");
        assertThat(file.getCreatedId()).isEqualTo(1001L);
        assertThat(file.getModifiedId()).isEqualTo(1001L);
        var uploadSession = uploadSessionRepository.findByUploadSessionNoAndDeleted(result.uploadSessionNo(), 0)
            .orElseThrow();
        assertThat(uploadSession.getStatus()).isEqualTo(FileUploadSessionStatus.CREATED);
        assertThat(uploadSession.getCreatedId()).isEqualTo(1001L);
        assertThat(uploadSession.getModifiedId()).isEqualTo(1001L);
    }

    @Test
    void createAudioUploadSessionUsesAudioDirectory() {
        FileService.UploadSessionResult result = fileService.createUploadSession(new FileService.CreateUploadSessionCommand(
            "intro.mp3",
            "audio/mpeg",
            FileCategory.AUDIO,
            4096,
            null,
            null,
            null
        ));

        assertThat(result.objectKey()).matches("local/audio/\\d{8}/" + result.fileNo() + "\\.mp3");
        assertThat(result.file().fileCategory()).isEqualTo(FileCategory.AUDIO);
    }

    @Test
    void completeFailsWhenObjectIsMissing() {
        FileService.UploadSessionResult result = createImageUploadSession(2048);

        assertThatThrownBy(() -> fileService.completeUploadSession(result.uploadSessionNo()))
            .isInstanceOf(BusinessException.class)
            .hasMessage("OSS 对象不存在");
    }

    @Test
    void completeValidatesOssObjectAndMarksFileAvailable() {
        FileService.UploadSessionResult result = createImageUploadSession(2048);
        objectStorageClient.putObject(result.bucket(), result.objectKey(), 2048, "image/jpeg", "etag-1");

        FileService.FileInfo file = fileService.completeUploadSession(result.uploadSessionNo());

        assertThat(file.status()).isEqualTo(FileStatus.AVAILABLE);
        assertThat(file.etag()).isEqualTo("etag-1");
        assertThat(uploadSessionRepository.findByUploadSessionNoAndDeleted(result.uploadSessionNo(), 0).orElseThrow().getStatus())
            .isEqualTo(FileUploadSessionStatus.COMPLETED);
    }

    @Test
    void previewUrlUsesSamePrivateAccessCheck() {
        FileService.UploadSessionResult result = createImageUploadSession(2048);
        objectStorageClient.putObject(result.bucket(), result.objectKey(), 2048, "image/jpeg", "etag-1");
        fileService.completeUploadSession(result.uploadSessionNo());

        FileService.DownloadUrlResult previewUrl = fileService.createPreviewUrl(result.fileNo());

        assertThat(previewUrl.fileNo()).isEqualTo(result.fileNo());
        assertThat(previewUrl.url()).contains("/download/cxc-commerce-file/");
    }

    @Test
    void nonUploaderCannotDownloadPrivateFile() {
        FileService.UploadSessionResult result = createImageUploadSession(2048);
        objectStorageClient.putObject(result.bucket(), result.objectKey(), 2048, "image/jpeg", "etag-1");
        fileService.completeUploadSession(result.uploadSessionNo());

        setAuth(2002L);

        assertThatThrownBy(() -> fileService.createDownloadUrl(result.fileNo()))
            .isInstanceOf(BusinessException.class)
            .hasMessage("无权访问文件");
    }

    @Test
    void deletedFileCannotBeDownloaded() {
        FileService.UploadSessionResult result = createImageUploadSession(2048);
        objectStorageClient.putObject(result.bucket(), result.objectKey(), 2048, "image/jpeg", "etag-1");
        fileService.completeUploadSession(result.uploadSessionNo());

        fileService.deleteFile(result.fileNo());

        assertThatThrownBy(() -> fileService.createDownloadUrl(result.fileNo()))
            .isInstanceOf(BusinessException.class)
            .hasMessage("文件不存在");
    }

    private FileService.UploadSessionResult createImageUploadSession(long sizeBytes) {
        return fileService.createUploadSession(new FileService.CreateUploadSessionCommand(
            "main.jpg",
            "image/jpeg",
            FileCategory.IMAGE,
            sizeBytes,
            null,
            800,
            600
        ));
    }

    private void setAuth(Long userId) {
        PrincipalContextHolder.set(new CurrentPrincipalContext(
            userId,
            userId + 10,
            userId + 20,
            "phone",
            "WECHAT_MINIAPP",
            "cxc-commerce-buyer-wechat-miniapp",
            "CLIENT"
        ));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = FileObjectPO.class)
    @EnableJpaRepositories(basePackageClasses = FileObjectRepository.class)
    @Import({
        FileDomainConfig.class,
        FileServiceImpl.class,
        FileNumberGenerator.class,
        FileObjectKeyBuilder.class,
        FileTypePolicy.class,
        CurrentPrincipal.class,
        TestStorageConfig.class
    })
    static class TestApplication {
    }

    static class TestStorageConfig {
        @Bean
        MockObjectStorageClient mockObjectStorageClient() {
            return new MockObjectStorageClient();
        }

        @Bean
        ObjectStorageBucketResolver objectStorageBucketResolver() {
            return appCode -> "cxc-commerce-file";
        }
    }
}
