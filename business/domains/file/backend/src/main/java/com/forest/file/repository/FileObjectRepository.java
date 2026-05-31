package com.forest.file.repository;

import com.forest.file.entity.FileObjectPO;
import com.forest.file.entity.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 文件元数据仓库。
 */
public interface FileObjectRepository extends JpaRepository<FileObjectPO, Long> {
    Optional<FileObjectPO> findByFileNoAndDeleted(String fileNo, Integer deleted);

    List<FileObjectPO> findByFileNoInAndDeleted(Collection<String> fileNos, Integer deleted);

    boolean existsByFileNo(String fileNo);

    List<FileObjectPO> findByStatusAndDeleted(FileStatus status, Integer deleted);
}
