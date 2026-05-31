package com.forest.user.user.avatar.service;

import com.forest.file.entity.FileCategory;
import com.forest.file.entity.FileObjectPO;
import com.forest.file.entity.FileStatus;
import com.forest.file.service.FileService;
import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.starter.exception.BusinessException;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证用户头像和文件域的协作规则。
 */
class UserAvatarServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final FileService fileService = mock(FileService.class);
    private final UserAvatarService service = new UserAvatarService(userRepository, fileService);

    @Test
    void updateCurrentAvatarStoresStableFileNo() {
        UserPO user = activeUser(1001L);
        String fileNo = "FILE20260511ABCDEF1234567890";
        when(userRepository.findById(1001L)).thenReturn(Optional.of(user));
        when(fileService.getFile(fileNo)).thenReturn(fileInfo(fileNo, FileCategory.IMAGE, FileStatus.AVAILABLE));
        when(userRepository.save(user)).thenReturn(user);

        UserPO result = service.updateCurrentAvatar(1001L, fileNo);

        assertThat(result.getAvatar()).isEqualTo(fileNo);
        verify(userRepository).save(user);
    }

    @Test
    void updateCurrentAvatarRejectsNonImageFile() {
        String fileNo = "FILE20260511ABCDEF1234567890";
        when(userRepository.findById(1001L)).thenReturn(Optional.of(activeUser(1001L)));
        when(fileService.getFile(fileNo)).thenReturn(fileInfo(fileNo, FileCategory.DOCUMENT, FileStatus.AVAILABLE));

        assertThatThrownBy(() -> service.updateCurrentAvatar(1001L, fileNo))
            .isInstanceOf(BusinessException.class)
            .hasMessage("头像必须是图片文件");
    }

    @Test
    void avatarUrlIgnoresNonFileNoValue() {
        assertThat(service.avatarFileNo("https://forest.example/avatar.png")).isNull();
        assertThat(service.avatarUrl("https://forest.example/avatar.png")).isNull();
    }

    @Test
    void avatarPolicyAllowsReferencedAvatarReadButKeepsDeletePrivate() {
        UserAvatarFileAccessPolicy policy = new UserAvatarFileAccessPolicy(userRepository);
        FileObjectPO file = new FileObjectPO();
        file.setFileNo("FILE20260511ABCDEF1234567890");
        file.setUploaderUserId(1001L);
        when(userRepository.existsByAvatarAndDeleted(file.getFileNo(), 0)).thenReturn(true);
        CurrentPrincipalContext otherUser = new CurrentPrincipalContext(
            2002L,
            2012L,
            2022L,
            "phone",
            "WECHAT_MINIAPP",
            "cxc-commerce-buyer-wechat-miniapp",
            "CLIENT"
        );

        assertThat(policy.canRead(otherUser, file)).isTrue();
        assertThat(policy.canDelete(otherUser, file)).isFalse();
    }

    private UserPO activeUser(Long userId) {
        UserPO user = new UserPO();
        user.setId(userId);
        user.setStatus(UserPO.Status.ACTIVE);
        return user;
    }

    private FileService.FileInfo fileInfo(String fileNo, FileCategory category, FileStatus status) {
        return new FileService.FileInfo(
            fileNo,
            "cxc-commerce",
            "cxc-commerce-buyer-wechat-miniapp",
            "avatar.png",
            "image/png",
            category,
            1024,
            "etag-1",
            status,
            LocalDateTime.now()
        );
    }
}
