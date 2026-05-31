package com.forest.file.repository;

import com.forest.file.entity.FileUploadSessionPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 文件上传会话仓库。
 */
public interface FileUploadSessionRepository extends JpaRepository<FileUploadSessionPO, Long> {
    Optional<FileUploadSessionPO> findByUploadSessionNo(String uploadSessionNo);

    Optional<FileUploadSessionPO> findByUploadSessionNoAndDeleted(String uploadSessionNo, Integer deleted);

    boolean existsByUploadSessionNo(String uploadSessionNo);
}
