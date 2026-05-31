package com.forest.starter.wechat.miniapp;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.wechat.config.ForestWechatProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Default HTTP implementation for WeChat miniapp login and phone number APIs.
 */
public class DefaultWechatMiniappClient implements WechatMiniappClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultWechatMiniappClient.class);

    private final ForestWechatProperties properties;
    private final RestTemplate restTemplate;
    private final JsonMapper objectMapper;
    private final Map<String, CachedAccessToken> accessTokens = new ConcurrentHashMap<>();

    public DefaultWechatMiniappClient(
        ForestWechatProperties properties,
        RestTemplate restTemplate,
        JsonMapper objectMapper
    ) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public WechatCodeSession codeToSession(String appCode, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("微信登录 code 不能为空");
        }
        ForestWechatProperties.Miniapp miniapp = requireMiniapp(appCode);
        if (miniapp.isMockEnabled()) {
            return new WechatCodeSession(buildMockOpenId(code), null, null);
        }
        requireRealMiniappCredentials(appCode, miniapp);

        String url = UriComponentsBuilder
            .fromUriString("https://api.weixin.qq.com/sns/jscode2session")
            .queryParam("appid", miniapp.getAppid().trim())
            .queryParam("secret", miniapp.getSecret().trim())
            .queryParam("js_code", code)
            .queryParam("grant_type", "authorization_code")
            .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            if (root.hasNonNull("errcode")) {
                throw new BusinessException("微信登录失败：" + root.path("errmsg").asString("unknown"));
            }
            String openId = root.path("openid").asString();
            if (openId == null || openId.isBlank()) {
                throw new BusinessException("微信登录失败，缺少 openid");
            }
            return new WechatCodeSession(
                openId,
                root.path("unionid").asString(null),
                root.path("session_key").asString(null)
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("微信登录响应解析失败", ex);
        }
    }

    @Override
    public WechatPhoneNumber getPhoneNumber(String appCode, String phoneCode) {
        if (phoneCode == null || phoneCode.isBlank()) {
            throw new BusinessException("微信手机号授权 code 不能为空");
        }
        ForestWechatProperties.Miniapp miniapp = requireMiniapp(appCode);
        if (miniapp.isMockEnabled()) {
            return new WechatPhoneNumber(phoneCode, phoneCode, "86");
        }
        requireRealMiniappCredentials(appCode, miniapp);

        String accessToken = fetchAccessToken(appCode, miniapp);
        String url = UriComponentsBuilder
            .fromUriString("https://api.weixin.qq.com/wxa/business/getuserphonenumber")
            .queryParam("access_token", accessToken)
            .toUriString();

        try {
            String response = restTemplate.postForObject(url, phoneCodeRequest(phoneCode), String.class);
            if (response == null || response.isBlank()) {
                log.warn("WeChat miniapp phone auth returned empty response. appCode={}", appCode);
                throw new BusinessException("微信手机号授权响应为空");
            }
            JsonNode root = objectMapper.readTree(response);
            if (root.path("errcode").asInt(0) != 0) {
                int errcode = root.path("errcode").asInt();
                String errmsg = root.path("errmsg").asString("unknown");
                log.warn(
                    "WeChat miniapp phone auth failed. appCode={}, errcode={}, errmsg={}",
                    appCode,
                    errcode,
                    errmsg
                );
                throw new BusinessException("微信手机号授权失败：" + errmsg);
            }
            JsonNode phoneInfo = root.path("phone_info");
            String phoneNumber = phoneInfo.path("phoneNumber").asString();
            String purePhoneNumber = phoneInfo.path("purePhoneNumber").asString();
            if (phoneNumber == null || phoneNumber.isBlank()) {
                phoneNumber = purePhoneNumber;
            }
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new BusinessException("微信手机号授权失败，缺少手机号");
            }
            return new WechatPhoneNumber(phoneNumber, purePhoneNumber, phoneInfo.path("countryCode").asString());
        } catch (BusinessException ex) {
            throw ex;
        } catch (HttpClientErrorException ex) {
            log.warn(
                "WeChat miniapp phone auth HTTP failed. appCode={}, status={}, responseBody={}",
                appCode,
                ex.getStatusCode(),
                ex.getResponseBodyAsString(),
                ex
            );
            throw new BusinessException("微信手机号授权 HTTP 请求失败：" + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.warn("WeChat miniapp phone auth response parse failed. appCode={}", appCode, ex);
            throw new BusinessException("微信手机号授权响应解析失败", ex);
        }
    }

    private ForestWechatProperties.Miniapp requireMiniapp(String appCode) {
        String safeAppCode = requireAppCode(appCode);
        ForestWechatProperties.Miniapp miniapp = properties.getMiniapps().get(safeAppCode);
        if (miniapp == null) {
            throw new BusinessException("微信小程序未配置：" + safeAppCode);
        }
        return miniapp;
    }

    private String requireAppCode(String appCode) {
        if (appCode == null || appCode.isBlank()) {
            throw new BusinessException("appCode 不能为空");
        }
        return appCode.trim();
    }

    private void requireRealMiniappCredentials(String appCode, ForestWechatProperties.Miniapp miniapp) {
        if (miniapp.getAppid() == null || miniapp.getAppid().isBlank()
            || miniapp.getSecret() == null || miniapp.getSecret().isBlank()) {
            throw new BusinessException("微信小程序 appid/secret 未配置：" + appCode);
        }
    }

    private String fetchAccessToken(String appCode, ForestWechatProperties.Miniapp miniapp) {
        CachedAccessToken cached = accessTokens.get(appCode);
        long now = System.currentTimeMillis();
        if (cached != null && cached.expiresAtMillis() > now) {
            return cached.token();
        }

        String url = UriComponentsBuilder
            .fromUriString("https://api.weixin.qq.com/cgi-bin/token")
            .queryParam("grant_type", "client_credential")
            .queryParam("appid", miniapp.getAppid().trim())
            .queryParam("secret", miniapp.getSecret().trim())
            .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            if (root.hasNonNull("errcode") && root.path("errcode").asInt() != 0) {
                throw new BusinessException("微信 access_token 获取失败：" + root.path("errmsg").asString("unknown"));
            }
            String accessToken = root.path("access_token").asString();
            if (accessToken == null || accessToken.isBlank()) {
                throw new BusinessException("微信 access_token 获取失败，缺少 access_token");
            }
            int expiresIn = root.path("expires_in").asInt(7200);
            long ttlMillis = Math.max(60, expiresIn - 60L) * 1000L;
            accessTokens.put(appCode, new CachedAccessToken(accessToken, now + ttlMillis));
            return accessToken;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("微信 access_token 响应解析失败", ex);
        }
    }

    private HttpEntity<String> phoneCodeRequest(String phoneCode) throws java.io.IOException {
        String body = objectMapper.writeValueAsString(Map.of("code", phoneCode));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentLength(body.getBytes(StandardCharsets.UTF_8).length);
        return new HttpEntity<>(body, headers);
    }

    private String buildMockOpenId(String code) {
        String sanitized = code.replaceAll("[^a-zA-Z0-9_-]", "");
        return "mock-openid-" + sanitized;
    }

    private record CachedAccessToken(String token, long expiresAtMillis) {
    }
}
