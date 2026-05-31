package com.forest.cxccommerce.config;

import com.forest.organization.workspace.web.OrganizationWorkspaceInterceptor;
import com.forest.organization.platform.PlatformAccessContextInterceptor;
import com.forest.starter.auth.TokenAuthInterceptor;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 CXC Commerce 路由拦截器。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final TokenAuthInterceptor tokenAuthInterceptor;
    private final OrganizationWorkspaceInterceptor organizationWorkspaceInterceptor;
    private final PlatformAccessContextInterceptor platformAccessContextInterceptor;

    public WebMvcConfig(
        TokenAuthInterceptor tokenAuthInterceptor,
        OrganizationWorkspaceInterceptor organizationWorkspaceInterceptor,
        PlatformAccessContextInterceptor platformAccessContextInterceptor
    ) {
        this.tokenAuthInterceptor = tokenAuthInterceptor;
        this.organizationWorkspaceInterceptor = organizationWorkspaceInterceptor;
        this.platformAccessContextInterceptor = platformAccessContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenAuthInterceptor)
            .addPathPatterns(ForestApiPaths.API + "/**")
            .excludePathPatterns(
                ForestApiPaths.AUTH + "/sms/send",
                ForestApiPaths.AUTH + "/phone/login",
                ForestApiPaths.AUTH + "/password/login",
                ForestApiPaths.AUTH + "/wechat-miniapp/login",
                ForestApiPaths.AUTH + "/wechat-miniapp/phone-login",
                ForestApiPaths.AUTH + "/refresh"
            );
        registry.addInterceptor(organizationWorkspaceInterceptor)
            .addPathPatterns(ForestApiPaths.ADMIN + "/workspace/**");
        registry.addInterceptor(platformAccessContextInterceptor)
            .addPathPatterns(ForestApiPaths.PLATFORM + "/**");
    }
}
