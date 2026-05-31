package com.forest.organization.workspace.context;

/**
 * 保存当前请求线程内的企业工作台上下文。
 *
 * <p>该 ThreadLocal 只服务于一次 HTTP 请求。写入方必须在 finally 中清理，
 * 避免线程复用时把上一次请求的企业上下文泄漏到下一次请求。</p>
 */
public final class OrganizationWorkspaceContextHolder {
    private static final ThreadLocal<OrganizationWorkspaceContext> CONTEXT = new ThreadLocal<>();

    private OrganizationWorkspaceContextHolder() {
    }

    public static void set(OrganizationWorkspaceContext context) {
        CONTEXT.set(context);
    }

    public static OrganizationWorkspaceContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
