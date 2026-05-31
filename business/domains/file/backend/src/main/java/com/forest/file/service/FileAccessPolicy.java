package com.forest.file.service;

import com.forest.file.entity.FileObjectPO;
import com.forest.starter.auth.context.CurrentPrincipalContext;

/**
 * 文件访问策略。
 *
 * <p>本期默认只允许上传者本人访问；后续平台、商家、组织和 RBAC 可通过替换该接口扩展。</p>
 */
public interface FileAccessPolicy {
    boolean canRead(CurrentPrincipalContext authContext, FileObjectPO file);

    boolean canDelete(CurrentPrincipalContext authContext, FileObjectPO file);
}
