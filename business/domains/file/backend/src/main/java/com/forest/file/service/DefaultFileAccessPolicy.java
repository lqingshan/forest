package com.forest.file.service;

import com.forest.file.entity.FileObjectPO;
import com.forest.starter.auth.context.CurrentPrincipalContext;

/**
 * 文件默认权限策略：上传者本人可读、可删。
 */
public class DefaultFileAccessPolicy implements FileAccessPolicy {
    @Override
    public boolean canRead(CurrentPrincipalContext authContext, FileObjectPO file) {
        return sameUploader(authContext, file);
    }

    @Override
    public boolean canDelete(CurrentPrincipalContext authContext, FileObjectPO file) {
        return sameUploader(authContext, file);
    }

    private boolean sameUploader(CurrentPrincipalContext authContext, FileObjectPO file) {
        return authContext != null
            && authContext.userId() != null
            && authContext.userId().equals(file.getUploaderUserId());
    }
}
