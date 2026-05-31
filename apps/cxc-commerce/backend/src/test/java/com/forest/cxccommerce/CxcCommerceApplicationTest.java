package com.forest.cxccommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证 CXC Commerce 后端最小认证装配。
 */
@SpringBootTest
@ActiveProfiles("test")
class CxcCommerceApplicationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void wechatMiniappLoginIsOpenAndCreatesSession() throws Exception {
        mockMvc.perform(post("/api/auth/wechat-miniapp/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "cxc-login",
                      "clientType": "WECHAT_MINIAPP",
                      "appCode": "cxc-commerce-buyer-wechat-miniapp",
                      "accessScope": "CLIENT"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken", not(blankOrNullString())))
            .andExpect(jsonPath("$.data.refreshToken", not(blankOrNullString())))
            .andExpect(jsonPath("$.data.appCode").value("cxc-commerce-buyer-wechat-miniapp"));
    }

    @Test
    void protectedApiRequiresToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("未登录或 Token 无效"));
    }
}
