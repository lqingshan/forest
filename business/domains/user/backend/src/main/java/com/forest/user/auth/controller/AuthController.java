package com.forest.user.auth.controller;

import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.starter.web.ClientIpResolver;
import com.forest.user.auth.command.CarrierOneClickLoginCommand;
import com.forest.user.auth.command.PhonePasswordLoginCommand;
import com.forest.user.auth.command.PhoneSmsLoginCommand;
import com.forest.user.auth.command.SendSmsCodeCommand;
import com.forest.user.auth.command.WechatMiniappLoginCommand;
import com.forest.user.auth.command.WechatMiniappPhoneLoginCommand;
import com.forest.user.auth.result.AuthLoginResult;
import com.forest.user.auth.result.AuthRefreshResult;
import com.forest.user.auth.result.SmsSendResult;
import com.forest.user.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露通用认证接口。
 */
@RestController
@RequestMapping(ForestApiPaths.AUTH)
public class AuthController {
    private final AuthService authService;
    private final CurrentPrincipal currentPrincipal;

    public AuthController(
        AuthService authService,
        CurrentPrincipal currentPrincipal
    ) {
        this.authService = authService;
        this.currentPrincipal = currentPrincipal;
    }

    @PostMapping("/sms/send")
    public Result<SmsSendResult> sendSms(
        @RequestBody SmsSendRequest request,
        HttpServletRequest httpRequest
    ) {
        return Result.success(authService.sendSmsCode(new SendSmsCodeCommand(
            request.phone(),
            request.clientType(),
            request.appCode(),
            request.accessScope(),
            ClientIpResolver.resolve(httpRequest)
        )));
    }

    @PostMapping("/phone/login")
    public Result<LoginVO> loginByPhone(@RequestBody PhoneLoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(LoginVO.from(authService.loginByPhoneSms(new PhoneSmsLoginCommand(
            request.phone(),
            request.smsCode(),
            request.clientType(),
            request.appCode(),
            request.accessScope(),
            ClientIpResolver.resolve(httpRequest),
            userAgent(httpRequest)
        ))));
    }

    @PostMapping("/password/login")
    public Result<LoginVO> loginByPassword(@RequestBody PasswordLoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(LoginVO.from(authService.loginByPhonePassword(new PhonePasswordLoginCommand(
            request.phone(),
            request.password(),
            request.clientType(),
            request.appCode(),
            request.accessScope(),
            ClientIpResolver.resolve(httpRequest),
            userAgent(httpRequest)
        ))));
    }

    @PostMapping("/carrier/one-click-login")
    public Result<LoginVO> loginByCarrierOneClick(
        @RequestBody CarrierOneClickLoginRequest request,
        HttpServletRequest httpRequest
    ) {
        return Result.success(LoginVO.from(authService.loginByCarrierToken(new CarrierOneClickLoginCommand(
            request.carrierToken(),
            request.provider(),
            request.clientType(),
            request.appCode(),
            request.accessScope(),
            ClientIpResolver.resolve(httpRequest),
            userAgent(httpRequest)
        ))));
    }

    @PostMapping("/wechat-miniapp/login")
    public Result<LoginVO> loginByWechatMiniapp(
        @RequestBody WechatMiniappLoginRequest request,
        HttpServletRequest httpRequest
    ) {
        return Result.success(LoginVO.from(authService.loginByWechatMiniapp(new WechatMiniappLoginCommand(
            request.code(),
            request.clientType(),
            request.appCode(),
            request.accessScope(),
            ClientIpResolver.resolve(httpRequest),
            userAgent(httpRequest)
        ))));
    }

    @PostMapping("/wechat-miniapp/phone-login")
    public Result<LoginVO> loginByWechatMiniappPhone(
        @RequestBody WechatMiniappPhoneLoginRequest request,
        HttpServletRequest httpRequest
    ) {
        return Result.success(LoginVO.from(authService.loginByWechatMiniappPhone(new WechatMiniappPhoneLoginCommand(
            request.code(),
            request.phoneCode(),
            request.clientType(),
            request.appCode(),
            request.accessScope(),
            ClientIpResolver.resolve(httpRequest),
            userAgent(httpRequest)
        ))));
    }

    @PostMapping("/refresh")
    public Result<AuthRefreshResult> refresh(@RequestBody RefreshRequest request) {
        return Result.success(authService.refreshToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    public Result<ActionVO> logout() {
        authService.logout(currentPrincipal.require().sessionId());
        return Result.success(new ActionVO(true));
    }

    @GetMapping("/me")
    public Result<CurrentPrincipalContext> me() {
        return Result.success(currentPrincipal.require());
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public record SmsSendRequest(String phone, String clientType, String appCode, String accessScope) {
    }

    public record PhoneLoginRequest(String phone, String smsCode, String clientType, String appCode, String accessScope) {
    }

    public record PasswordLoginRequest(String phone, String password, String clientType, String appCode, String accessScope) {
    }

    public record CarrierOneClickLoginRequest(String carrierToken, String provider, String clientType, String appCode, String accessScope) {
    }

    public record WechatMiniappLoginRequest(String code, String clientType, String appCode, String accessScope) {
    }

    public record WechatMiniappPhoneLoginRequest(String code, String phoneCode, String clientType, String appCode, String accessScope) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record LoginVO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        String clientType,
        String appCode,
        String accessScope,
        boolean firstLogin
    ) {
        public static LoginVO from(AuthLoginResult result) {
            return new LoginVO(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn(),
                result.refreshExpiresIn(),
                result.clientType(),
                result.appCode(),
                result.accessScope(),
                result.firstLogin()
            );
        }

    }

    public record ActionVO(boolean success) {
    }
}
