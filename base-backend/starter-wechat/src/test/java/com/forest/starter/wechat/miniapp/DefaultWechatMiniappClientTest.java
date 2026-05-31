package com.forest.starter.wechat.miniapp;

import java.util.LinkedHashMap;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.json.ForestObjectMappers;
import com.forest.starter.wechat.config.ForestWechatProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DefaultWechatMiniappClientTest {
    @Test
    void codeToSessionUsesMiniappConfigByAppCode() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        WechatMiniappClient client = client(restTemplate, "buyer-miniapp", "wx-buyer", "buyer-secret", false);

        server.expect(requestTo(containsString("https://api.weixin.qq.com/sns/jscode2session")))
            .andExpect(requestTo(containsString("appid=wx-buyer")))
            .andExpect(requestTo(containsString("secret=buyer-secret")))
            .andExpect(requestTo(containsString("js_code=login-code")))
            .andRespond(withSuccess("{\"openid\":\"openid-buyer\",\"unionid\":\"union\",\"session_key\":\"session\"}", MediaType.APPLICATION_JSON));

        WechatCodeSession session = client.codeToSession("buyer-miniapp", "login-code");

        assertEquals("openid-buyer", session.openId());
        assertEquals("union", session.unionId());
        assertEquals("session", session.sessionKey());
        server.verify();
    }

    @Test
    void getPhoneNumberUsesAccessTokenForSameAppCodeAndCachesIt() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        WechatMiniappClient client = client(restTemplate, "buyer-miniapp", "wx-buyer", "buyer-secret", false);

        server.expect(requestTo(containsString("https://api.weixin.qq.com/cgi-bin/token")))
            .andExpect(requestTo(containsString("appid=wx-buyer")))
            .andExpect(requestTo(containsString("secret=buyer-secret")))
            .andRespond(withSuccess("{\"access_token\":\"access-token\",\"expires_in\":7200}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("https://api.weixin.qq.com/wxa/business/getuserphonenumber")))
            .andExpect(requestTo(containsString("access_token=access-token")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_JSON_VALUE)))
            .andExpect(header(HttpHeaders.CONTENT_LENGTH, "23"))
            .andExpect(content().json("{\"code\":\"phone-code-1\"}"))
            .andRespond(withSuccess("{\"errcode\":0,\"phone_info\":{\"phoneNumber\":\"13800138000\",\"purePhoneNumber\":\"13800138000\",\"countryCode\":\"86\"}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("https://api.weixin.qq.com/wxa/business/getuserphonenumber")))
            .andExpect(requestTo(containsString("access_token=access-token")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_JSON_VALUE)))
            .andExpect(header(HttpHeaders.CONTENT_LENGTH, "23"))
            .andExpect(content().json("{\"code\":\"phone-code-2\"}"))
            .andRespond(withSuccess("{\"errcode\":0,\"phone_info\":{\"phoneNumber\":\"13800138001\",\"purePhoneNumber\":\"13800138001\",\"countryCode\":\"86\"}}", MediaType.APPLICATION_JSON));

        assertEquals("13800138000", client.getPhoneNumber("buyer-miniapp", "phone-code-1").phoneNumber());
        assertEquals("13800138001", client.getPhoneNumber("buyer-miniapp", "phone-code-2").phoneNumber());
        server.verify();
    }

    @Test
    void mockEnabledDoesNotCallWechat() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        WechatMiniappClient client = client(restTemplate, "buyer-miniapp", "", "", true);

        assertEquals("mock-openid-wx-code", client.codeToSession("buyer-miniapp", "wx-code").openId());
        assertEquals("13800138000", client.getPhoneNumber("buyer-miniapp", "13800138000").phoneNumber());
        server.verify();
    }

    @Test
    void rejectsUnknownMiniappConfig() {
        RestTemplate restTemplate = new RestTemplate();
        WechatMiniappClient client = client(restTemplate, "buyer-miniapp", "wx-buyer", "buyer-secret", false);

        assertBusinessMessage("微信小程序未配置：missing-miniapp", () -> client.codeToSession("missing-miniapp", "code"));
    }

    private WechatMiniappClient client(
        RestTemplate restTemplate,
        String appCode,
        String appid,
        String secret,
        boolean mockEnabled
    ) {
        ForestWechatProperties.Miniapp miniapp = new ForestWechatProperties.Miniapp();
        miniapp.setAppid(appid);
        miniapp.setSecret(secret);
        miniapp.setMockEnabled(mockEnabled);
        ForestWechatProperties wechatProperties = new ForestWechatProperties();
        wechatProperties.setMiniapps(new LinkedHashMap<>());
        wechatProperties.getMiniapps().put(appCode, miniapp);

        return new DefaultWechatMiniappClient(
            wechatProperties,
            restTemplate,
            ForestObjectMappers.defaultJsonMapper()
        );
    }

    private void assertBusinessMessage(String message, Runnable action) {
        BusinessException ex = assertThrows(BusinessException.class, action::run);
        assertEquals(message, ex.getMessage());
    }
}
