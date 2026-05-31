package com.forest.user.user.avatar.service;

import com.forest.file.entity.FileObjectPO;
import com.forest.file.service.FileAccessPolicy;
import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.user.user.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 用户域扩展的文件访问策略。
 *
 * <p>普通文件仍然遵循上传者本人可读、可删；一旦某个文件被作为用户头像引用，
 * 同一业务 app 内的已登录用户都可以读取，便于资料卡、头像列表等场景展示。
 * 删除权限不会因为头像可读而放大。</p>
 */
@Primary
@Component
public class UserAvatarFileAccessPolicy implements FileAccessPolicy {
    private final UserRepository userRepository;

    /**
     * 注入用户仓储，用于判断文件是否已经被某个用户作为头像引用。
     */
    public UserAvatarFileAccessPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 判断当前登录用户是否可以读取文件。
     *
     * <p>规则分两层：上传者本人始终可读；如果文件已经被任意用户作为头像引用，
     * 则同一业务 app 内的已登录用户也可以读取，用于头像展示、人员列表等公共展示场景。</p>
     */
    @Override
    public boolean canRead(CurrentPrincipalContext authContext, FileObjectPO file) {
        return sameUploader(authContext, file) || avatarVisibleToLoggedUser(authContext, file);
    }

    /**
     * 判断当前登录用户是否可以删除文件。
     *
     * <p>删除权限不随头像可读范围扩大，仍然只允许上传者本人删除，
     * 避免普通用户因为能看到头像而具备删除他人头像文件的能力。</p>
     */
    @Override
    public boolean canDelete(CurrentPrincipalContext authContext, FileObjectPO file) {
        return sameUploader(authContext, file);
    }

    /**
     * 判断文件是否因为“被用作头像”而允许已登录用户读取。
     *
     * <p>这里仅检查当前请求存在登录 user，并且 {@code app_user.avatar} 中存在该 fileNo；
     * 具体文件可用性、业务 app 归属等校验仍由文件域主流程负责。</p>
     */
    private boolean avatarVisibleToLoggedUser(CurrentPrincipalContext authContext, FileObjectPO file) {
        return authContext != null
            && authContext.userId() != null
            && file != null
            && userRepository.existsByAvatarAndDeleted(file.getFileNo(), 0);
    }

    /**
     * 判断当前登录用户是否为文件上传者本人。
     */
    private boolean sameUploader(CurrentPrincipalContext authContext, FileObjectPO file) {
        return authContext != null
            && authContext.userId() != null
            && file != null
            && authContext.userId().equals(file.getUploaderUserId());
    }
}
