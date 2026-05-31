package com.forest.tradeleads.config;

import com.forest.starter.auth.TokenAuthInterceptor;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册外贸线索路由拦截器。
 *
 * <p>小程序登录、刷新 token 和微信支付回调是开放入口，其余 client API 依赖 JWT 身份。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final TokenAuthInterceptor tokenAuthInterceptor;

    public WebMvcConfig(TokenAuthInterceptor tokenAuthInterceptor) {
        this.tokenAuthInterceptor = tokenAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenAuthInterceptor)
            .addPathPatterns(ForestApiPaths.API + "/**")
            .excludePathPatterns(
                // 登录/刷新/微信支付回调是建立身份或外部系统调用的入口，不能依赖普通用户 JWT。
                ForestApiPaths.AUTH + "/sms/send",
                ForestApiPaths.AUTH + "/phone/login",
                ForestApiPaths.AUTH + "/password/login",
                ForestApiPaths.AUTH + "/wechat-miniapp/login",
                ForestApiPaths.AUTH + "/wechat-miniapp/phone-login",
                ForestApiPaths.AUTH + "/refresh",
                ForestApiPaths.OPEN + "/wechat/pay/notify"
            );
    }
}
