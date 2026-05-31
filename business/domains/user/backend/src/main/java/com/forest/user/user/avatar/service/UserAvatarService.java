package com.forest.user.user.avatar.service;

import com.forest.file.entity.FileCategory;
import com.forest.file.entity.FileStatus;
import com.forest.file.service.FileService;
import com.forest.starter.exception.BusinessException;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.regex.Pattern;

/**
 * 维护用户头像与文件域之间的协作关系。
 *
 * <p>{@code app_user.avatar} 只保存稳定 {@code fileNo}，接口展示时再临时换取
 * {@code avatarUrl}。这个服务只在 user 域引用 file 域，file 域不反向感知用户业务。</p>
 */
@Service
public class UserAvatarService {
    private static final Logger log = LoggerFactory.getLogger(UserAvatarService.class);
    private static final Pattern FILE_NO_PATTERN = Pattern.compile("^FILE[0-9]{8}[0-9A-F]{16}$");

    private final UserRepository userRepository;
    private final FileService fileService;

    public UserAvatarService(UserRepository userRepository, FileService fileService) {
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    /**
     * 将当前用户头像更新为指定图片文件。
     */
    @Transactional
    public UserPO updateCurrentAvatar(Long userId, String fileNo) {
        String safeFileNo = requireFileNo(fileNo);
        UserPO user = requireActiveUser(userId);
        FileService.FileInfo file = fileService.getFile(safeFileNo);
        requireImageAvailable(file);

        String previousAvatar = user.getAvatar();
        user.setAvatar(safeFileNo);
        user.setModifiedId(userId);
        UserPO savedUser = userRepository.save(user);

        String previousFileNo = avatarFileNo(previousAvatar);
        if (previousFileNo != null && !previousFileNo.equals(safeFileNo)) {
            deleteOldAvatarAfterCommit(previousFileNo);
        }
        return savedUser;
    }

    /**
     * 从头像原始值中提取稳定 fileNo。
     */
    public String avatarFileNo(String avatarValue) {
        String value = normalize(avatarValue);
        return value != null && FILE_NO_PATTERN.matcher(value).matches() ? value : null;
    }

    /**
     * 为头像生成临时展示 URL。
     *
     * <p>只有合法 fileNo 会通过文件域换取短期 preview URL。解析失败时返回 {@code null}，
     * 避免头像展示影响用户资料主流程。</p>
     */
    public String avatarUrl(String avatarValue) {
        String value = normalize(avatarValue);
        if (value == null) {
            return null;
        }
        String fileNo = avatarFileNo(value);
        if (fileNo == null) {
            return null;
        }
        try {
            return fileService.createPreviewUrl(fileNo).url();
        } catch (RuntimeException ex) {
            log.warn("Resolve avatar preview URL failed, fileNo={}", fileNo, ex);
            return null;
        }
    }

    private void requireImageAvailable(FileService.FileInfo file) {
        if (file.fileCategory() != FileCategory.IMAGE) {
            throw new BusinessException("头像必须是图片文件");
        }
        if (file.status() != FileStatus.AVAILABLE) {
            throw new BusinessException("头像文件不可用");
        }
    }

    private UserPO requireActiveUser(Long userId) {
        UserPO user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getStatus() == UserPO.Status.FROZEN) {
            throw new BusinessException("用户已被冻结");
        }
        if (user.getStatus() == UserPO.Status.DISABLED) {
            throw new BusinessException("用户已被禁用");
        }
        return user;
    }

    private void deleteOldAvatarBestEffort(String oldFileNo) {
        try {
            fileService.deleteFile(oldFileNo);
        } catch (RuntimeException ex) {
            log.warn("Delete old avatar failed, fileNo={}", oldFileNo, ex);
        }
    }

    private void deleteOldAvatarAfterCommit(String oldFileNo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteOldAvatarBestEffort(oldFileNo);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteOldAvatarBestEffort(oldFileNo);
            }
        });
    }

    private String requireFileNo(String fileNo) {
        String value = normalize(fileNo);
        if (value == null || !FILE_NO_PATTERN.matcher(value).matches()) {
            throw new BusinessException("头像文件编号无效");
        }
        return value;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
