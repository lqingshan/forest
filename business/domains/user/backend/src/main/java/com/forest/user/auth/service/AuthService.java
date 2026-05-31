package com.forest.user.auth.service;

import com.forest.user.auth.command.CarrierOneClickLoginCommand;
import com.forest.user.auth.command.PhonePasswordLoginCommand;
import com.forest.user.auth.command.PhoneSmsLoginCommand;
import com.forest.user.auth.command.SendSmsCodeCommand;
import com.forest.user.auth.command.WechatMiniappLoginCommand;
import com.forest.user.auth.command.WechatMiniappPhoneLoginCommand;
import com.forest.user.auth.result.AuthLoginResult;
import com.forest.user.auth.result.AuthRefreshResult;
import com.forest.user.auth.result.SmsSendResult;

/**
 * 统一认证 API 编排服务。
 *
 * <p>本接口只表达不同登录能力，不按 client/admin/platform 拆分服务。
 * accessScope 只写入 session/token，并由认证拦截器用于 API 前缀隔离；
 * 业务准入、成员身份和 RBAC 权限由对应业务域处理。</p>
 */
public interface AuthService {
    SmsSendResult sendSmsCode(SendSmsCodeCommand command);

    AuthLoginResult loginByPhoneSms(PhoneSmsLoginCommand command);

    AuthLoginResult loginByPhonePassword(PhonePasswordLoginCommand command);

    AuthLoginResult loginByCarrierToken(CarrierOneClickLoginCommand command);

    AuthLoginResult loginByWechatMiniapp(WechatMiniappLoginCommand command);

    AuthLoginResult loginByWechatMiniappPhone(WechatMiniappPhoneLoginCommand command);

    AuthRefreshResult refreshToken(String refreshToken);

    void logout(Long sessionId);
}
