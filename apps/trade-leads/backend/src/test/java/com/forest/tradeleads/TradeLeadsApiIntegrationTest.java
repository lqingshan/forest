package com.forest.tradeleads;

import com.forest.lead.entity.LeadPO;
import com.forest.lead.repository.LeadRepository;
import com.forest.lead.repository.LeadUnlockRecordRepository;
import com.forest.payment.repository.PaymentOrderRepository;
import com.forest.point.entity.PointBalancePO;
import com.forest.point.entity.PointLogPO;
import com.forest.point.repository.PointBalanceRepository;
import com.forest.point.repository.PointLogRepository;
import com.forest.point.service.PointBalanceService;
import com.forest.recharge.repository.RechargeOrderRepository;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.password.PasswordSecretCodec;
import com.forest.user.account.repository.AccountRepository;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import com.forest.user.useraccount.entity.UserAccountPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
/**
 * 基于组装后的应用验证外贸线索主要接口流程。
 */
class TradeLeadsApiIntegrationTest {
    private static final String PLATFORM_PASSWORD_ACCOUNT_TYPE = "platform_password";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadUnlockRecordRepository leadUnlockRecordRepository;

    @Autowired
    private PointBalanceRepository pointBalanceRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Autowired
    private RechargeOrderRepository rechargeOrderRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PointBalanceService pointBalanceService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PasswordSecretCodec passwordSecretCodec;

    private final JsonMapper objectMapper = JsonMapper.builder().build();
    private static final String DEFAULT_ADMIN_PHONE = "+8618257147892";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456abc";

    @BeforeEach
    void cleanDatabase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        paymentOrderRepository.deleteAll();
        rechargeOrderRepository.deleteAll();
        leadUnlockRecordRepository.deleteAll();
        pointLogRepository.deleteAll();
        pointBalanceRepository.deleteAll();
        leadRepository.deleteAll();
        userAccountRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        seedDefaultAdmin();
    }

    @Test
    void wechatLoginCreatesBusinessUserAndInitialBalance() throws Exception {
        JsonNode loginData = login("login_case");
        long accountId = loginData.path("accountId").asLong();
        long userId = loginData.path("userId").asLong();

        assertTrue(loginData.path("firstLogin").asBoolean());
        assertTrue(accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("wechat_miniapp", "trade-leads-miniapp", "mock-openid-login_case")
            .isPresent());
        assertEquals(userId, userAccountRepository.findByAccountId(accountId).orElseThrow().getUserId());
        assertEquals(0, pointBalanceRepository.findByUserId(userId).orElseThrow().getBalance());
    }

    @Test
    void adminLoginDoesNotInitializePointBalance() throws Exception {
        JsonNode loginData = adminLogin();
        long userId = loginData.path("principal").path("userId").asLong();

        assertTrue(pointBalanceRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void refreshTokenDoesNotInitializePointBalance() throws Exception {
        JsonNode loginData = login("refresh_event_case");
        long userId = loginData.path("userId").asLong();
        String refreshToken = loginData.path("refreshToken").asText();
        pointBalanceRepository.deleteAll();

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        assertTrue(pointBalanceRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void authenticatedEndpointsReturnPackagesAndBalance() throws Exception {
        JsonNode loginData = login("packages_case");
        long userId = loginData.path("userId").asLong();
        String accessToken = loginData.path("accessToken").asText();

        UserPO user = userRepository.findById(userId).orElseThrow();
        user.setAvatar("https://forest.example/avatar.png");
        user.setPhone("13800138000");
        user.setEmail("buyer@forest.example");
        userRepository.save(user);

        mockMvc.perform(get("/api/client/point/balance")
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.balance").value(0))
            .andExpect(jsonPath("$.data.totalIncome").value(0))
            .andExpect(jsonPath("$.data.totalSpend").value(0));

        mockMvc.perform(get("/api/client/recharge/packages")
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].code").value("starter"))
            .andExpect(jsonPath("$.data[0].amountCents").value(1))
            .andExpect(jsonPath("$.data[0].creditedPoints").value(99))
            .andExpect(jsonPath("$.data[1].code").value("growth"))
            .andExpect(jsonPath("$.data[2].code").value("pro"));

        mockMvc.perform(get("/api/client/user/me")
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(userId))
            .andExpect(jsonPath("$.data.userId").doesNotExist())
            .andExpect(jsonPath("$.data.avatar").isEmpty())
            .andExpect(jsonPath("$.data.avatarUrl").isEmpty())
            .andExpect(jsonPath("$.data.phone").value("13800138000"))
            .andExpect(jsonPath("$.data.email").value("buyer@forest.example"))
            .andExpect(jsonPath("$.data.adminUser").value(true))
            .andExpect(jsonPath("$.data.user").value(true));
    }

    @Test
    void unlockLeadConsumesPointsAndRevealsSensitiveFields() throws Exception {
        JsonNode loginData = login("unlock_case");
        long userId = loginData.path("userId").asLong();
        String accessToken = loginData.path("accessToken").asText();

        LeadPO lead = new LeadPO();
        lead.setName("Forest Supplier");
        lead.setCategory("Furniture");
        lead.setCountry("CN");
        lead.setPhone("13800138000");
        lead.setEmail("lead@forest.example");
        lead.setWebsite("https://forest.example");
        lead.setIntro("A sample lead for interface testing.");
        lead.setDeleted(0);
        lead = leadRepository.save(lead);

        pointBalanceService.addPoints(userId, 20, PointLogPO.SourceType.RECHARGE, 1L, "seed:" + userId);

        mockMvc.perform(get("/api/client/user-lead/{id}", lead.getId())
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.unlocked").value(false))
            .andExpect(jsonPath("$.data.phone").value("已遮挡，解锁后可见"))
            .andExpect(jsonPath("$.data.email").value("已遮挡，解锁后可见"))
            .andExpect(jsonPath("$.data.website").value("已遮挡，解锁后可见"));

        mockMvc.perform(post("/api/client/user-lead/{id}/unlock", lead.getId())
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.balanceAfter").value(15));

        mockMvc.perform(get("/api/client/user-lead/{id}", lead.getId())
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.unlocked").value(true))
            .andExpect(jsonPath("$.data.phone").value("13800138000"))
            .andExpect(jsonPath("$.data.email").value("lead@forest.example"))
            .andExpect(jsonPath("$.data.website").value("https://forest.example"));

        mockMvc.perform(get("/api/client/user-lead/unlocked/page")
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].id").value(lead.getId()))
            .andExpect(jsonPath("$.data.content[0].unlocked").value(true))
            .andExpect(jsonPath("$.data.content[0].phone").value("13800138000"))
            .andExpect(jsonPath("$.data.content[0].website").value("https://forest.example"));

        assertTrue(leadUnlockRecordRepository.findByUserIdAndLeadId(userId, lead.getId()).isPresent());
    }

    @Test
    void frozenUserCannotLoginRefreshOrAccessProtectedApis() throws Exception {
        JsonNode loginData = login("frozen_case");
        long userId = loginData.path("userId").asLong();
        String accessToken = loginData.path("accessToken").asText();
        String refreshToken = loginData.path("refreshToken").asText();

        UserPO user = userRepository.findById(userId).orElseThrow();
        user.setStatus(UserPO.Status.FROZEN);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/wechat-miniapp/phone-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "code", "frozen_case",
                    "phoneCode", phoneCodeFor("frozen_case"),
                    "clientType", "WECHAT_MINIAPP",
                    "appCode", "trade-leads-miniapp",
                    "accessScope", "CLIENT"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("用户已被冻结"));

        mockMvc.perform(get("/api/client/point/balance")
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("用户已被冻结"));

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("用户已被冻结"));
    }

    @Test
    void unlockLeadWithoutEnoughPointsReturnsBusinessError() throws Exception {
        JsonNode loginData = login("unlock_no_balance_case");
        long userId = loginData.path("userId").asLong();
        String accessToken = loginData.path("accessToken").asText();

        LeadPO lead = new LeadPO();
        lead.setName("Need Points Supplier");
        lead.setCategory("Lighting");
        lead.setCountry("CN");
        lead.setPhone("13900139000");
        lead.setEmail("need-points@forest.example");
        lead.setWebsite("https://need-points.example");
        lead.setIntro("LeadPO requires points to unlock.");
        lead.setDeleted(0);
        lead = leadRepository.save(lead);

        mockMvc.perform(post("/api/client/user-lead/{id}/unlock", lead.getId())
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("积分不足"));

        assertEquals(0, pointBalanceRepository.findByUserId(userId).orElseThrow().getBalance());
        assertTrue(leadUnlockRecordRepository.findByUserIdAndLeadId(userId, lead.getId()).isEmpty());
    }

    @Test
    void paymentNotifyTwiceDoesNotDoubleCreditBalance() throws Exception {
        JsonNode loginData = login("recharge_repeat_case");
        String accessToken = loginData.path("accessToken").asText();

        JsonNode orderData = createOrder(accessToken, "starter");
        JsonNode paymentData = createPaymentOrder(accessToken, "RECHARGE", orderData.path("id").asLong());
        String paymentNo = paymentData.path("paymentNo").asText();
        String outTradeNo = paymentData.path("outTradeNo").asText();
        int amountCents = paymentData.path("amountCents").asInt();

        assertTrue(paymentNo.matches("^PAY-\\d{14}-\\d{6}$"));
        assertTrue(outTradeNo.matches("^WX-\\d{14}-\\d{6}$"));
        assertTrue(outTradeNo.length() <= 32);
        assertEquals("PREPAY_CREATED", paymentData.path("status").asText());
        assertEquals("mock-pay-sign", paymentData.path("paymentParams").path("paySign").asText());
        assertTrue(paymentData.path("paymentParams").path("packageValue").asText().startsWith("prepay_id=mock-prepay-"));

        mockMvc.perform(post("/api/open/wechat/pay/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "outTradeNo", outTradeNo,
                    "transactionId", "mock-tx-1",
                    "amountCents", amountCents
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("success"));

        mockMvc.perform(post("/api/open/wechat/pay/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "outTradeNo", outTradeNo,
                    "transactionId", "mock-tx-1",
                    "amountCents", amountCents
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("success"));

        mockMvc.perform(get("/api/client/recharge/orders/{id}", orderData.path("id").asLong())
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.status").value("PAID"));

        mockMvc.perform(get("/api/client/point/balance")
                .header("Authorization", bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.balance").value(99))
            .andExpect(jsonPath("$.data.totalIncome").value(99))
            .andExpect(jsonPath("$.data.totalSpend").value(0));

        assertEquals(1L, pointLogRepository.count());
    }

    @Test
    void backendExposesBusinessAdminEndpointsInSingleRuntime() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        UserPO user = new UserPO();
        user.setName("Admin Query UserPO");
        user.setAvatar("https://forest.example/admin-query.png");
        user.setPhone("13900139000");
        user.setEmail("admin-query@forest.example");
        user = userRepository.save(user);
        pointBalanceService.addPoints(user.getId(), 12, PointLogPO.SourceType.RECHARGE, 1L, "admin-query:" + user.getId());

        LeadPO lead = new LeadPO();
        lead.setName("Admin LeadPO");
        lead.setCategory("Lighting");
        lead.setCountry("CN");
        lead.setPhone("13600136000");
        lead.setEmail("admin-lead@forest.example");
        lead.setWebsite("https://admin-lead.example");
        lead.setIntro("LeadPO exposed through business admin api.");
        lead.setDeleted(0);
        lead = leadRepository.save(lead);

        mockMvc.perform(get("/api/platform/user/page")
                .header("Authorization", bearer(adminToken))
                .param("name", "query user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content[0].id").value(user.getId()))
            .andExpect(jsonPath("$.data.content[0].name").value("Admin Query UserPO"))
            .andExpect(jsonPath("$.data.content[0].phone").value("13900139000"))
            .andExpect(jsonPath("$.data.content[0].email").value("admin-query@forest.example"))
            .andExpect(jsonPath("$.data.content[0].adminUser").value(true))
            .andExpect(jsonPath("$.data.content[0].user").value(true));

        mockMvc.perform(get("/api/platform/user/{id}", user.getId())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(user.getId()))
            .andExpect(jsonPath("$.data.adminUser").value(true))
            .andExpect(jsonPath("$.data.user").value(true));

        mockMvc.perform(get("/api/platform/user-point/page")
                .header("Authorization", bearer(adminToken))
                .param("name", "query user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content[0].user.id").value(user.getId()))
            .andExpect(jsonPath("$.data.content[0].user.name").value("Admin Query UserPO"))
            .andExpect(jsonPath("$.data.content[0].user.phone").value("13900139000"))
            .andExpect(jsonPath("$.data.content[0].user.email").value("admin-query@forest.example"))
            .andExpect(jsonPath("$.data.content[0].user.adminUser").value(true))
            .andExpect(jsonPath("$.data.content[0].user.user").value(true))
            .andExpect(jsonPath("$.data.content[0].points.balance").value(12))
            .andExpect(jsonPath("$.data.content[0].points.totalIncome").value(12))
            .andExpect(jsonPath("$.data.content[0].points.totalSpend").value(0));

        mockMvc.perform(get("/api/platform/user-point/{id}", user.getId())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.user.id").value(user.getId()))
            .andExpect(jsonPath("$.data.points.balance").value(12));

        mockMvc.perform(get("/api/platform/user-point/{id}/logs/page", user.getId())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content[0].userId").value(user.getId()))
            .andExpect(jsonPath("$.data.content[0].amount").value(12));

        mockMvc.perform(get("/api/platform/lead/{id}", lead.getId())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(lead.getId()))
            .andExpect(jsonPath("$.data.name").value("Admin LeadPO"));
    }

    @Test
    void userPointsPageAllowsEmptyQueryAndOrdersByPointModifiedTimeDesc() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        UserPO oldestUser = new UserPO();
        oldestUser.setName("Oldest Points User");
        oldestUser = userRepository.save(oldestUser);

        UserPO middleUser = new UserPO();
        middleUser.setName("Middle Points User");
        middleUser = userRepository.save(middleUser);

        UserPO newestUser = new UserPO();
        newestUser.setName("Newest Points User");
        newestUser = userRepository.save(newestUser);

        PointBalancePO oldestBalance = pointBalanceService.ensureBalance(oldestUser.getId());
        PointBalancePO middleBalance = pointBalanceService.ensureBalance(middleUser.getId());
        PointBalancePO newestBalance = pointBalanceService.ensureBalance(newestUser.getId());

        LocalDateTime sharedModifiedTime = LocalDateTime.of(2026, 4, 14, 12, 0);

        addBalanceWithModifiedTime(oldestBalance, 5, sharedModifiedTime.minusHours(2));
        addBalanceWithModifiedTime(middleBalance, 8, sharedModifiedTime);
        addBalanceWithModifiedTime(newestBalance, 11, sharedModifiedTime);

        mockMvc.perform(get("/api/platform/user-point/page")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.content[0].user.id").value(newestUser.getId()))
            .andExpect(jsonPath("$.data.content[1].user.id").value(middleUser.getId()))
            .andExpect(jsonPath("$.data.content[2].user.id").value(oldestUser.getId()))
            .andExpect(jsonPath("$.data.content[0].points.updatedAt").isNotEmpty());
    }

    private void addBalanceWithModifiedTime(PointBalancePO balance, int amount, LocalDateTime modifiedTime) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> assertEquals(
            1,
            pointBalanceRepository.addBalance(balance.getUserId(), amount, balance.getVersion(), modifiedTime)
        ));
    }

    @Test
    void userPointsPageRejectsShortPhoneOrEmailKeyword() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        mockMvc.perform(get("/api/platform/user-point/page")
                .header("Authorization", bearer(adminToken))
                .param("phone", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("手机号或邮箱至少输入 2 个字符"));
    }

    @Test
    void adminUserPageSupportsPhoneEmailSearchAndSizeTen() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        for (int i = 0; i < 12; i++) {
            UserPO user = new UserPO();
            user.setName("Bulk Query User " + i);
            user.setPhone(String.format("188000000%02d", i));
            user.setEmail(String.format("bulk%02d@forest.example", i));
            userRepository.save(user);
        }

        mockMvc.perform(get("/api/platform/user/page")
                .header("Authorization", bearer(adminToken))
                .param("phone", "188")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value(12))
            .andExpect(jsonPath("$.data.content.length()").value(10));

        mockMvc.perform(get("/api/platform/user/page")
                .header("Authorization", bearer(adminToken))
                .param("email", "BULK01@")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].email").value("bulk01@forest.example"));

        mockMvc.perform(get("/api/platform/user/page")
                .header("Authorization", bearer(adminToken))
                .param("phone", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("手机号或邮箱至少输入 2 个字符"));
    }

    @Test
    void leadPageOrdersByModifiedTimeDescThenCreatedTimeDesc() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        LeadPO oldestLead = new LeadPO();
        oldestLead.setName("Oldest LeadPO");
        oldestLead.setCategory("Machinery");
        oldestLead.setCountry("CN");
        oldestLead.setDeleted(0);
        oldestLead = leadRepository.save(oldestLead);

        LeadPO middleLead = new LeadPO();
        middleLead.setName("Middle LeadPO");
        middleLead.setCategory("Furniture");
        middleLead.setCountry("CN");
        middleLead.setDeleted(0);
        middleLead = leadRepository.save(middleLead);

        LeadPO newestLead = new LeadPO();
        newestLead.setName("Newest LeadPO");
        newestLead.setCategory("Lighting");
        newestLead.setCountry("CN");
        newestLead.setDeleted(0);
        newestLead = leadRepository.save(newestLead);

        oldestLead.setIntro("updated later");
        oldestLead = leadRepository.save(oldestLead);

        mockMvc.perform(get("/api/platform/lead/page")
                .header("Authorization", bearer(adminToken))
                .param("country", "CN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content[0].id").value(oldestLead.getId()))
            .andExpect(jsonPath("$.data.content[1].id").value(newestLead.getId()))
            .andExpect(jsonPath("$.data.content[2].id").value(middleLead.getId()));
    }

    @Test
    void adminActivateEndpointRestoresFrozenUser() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        UserPO user = new UserPO();
        user.setName("Recoverable UserPO");
        user.setStatus(UserPO.Status.FROZEN);
        user = userRepository.save(user);

        mockMvc.perform(post("/api/platform/user/{id}/activate", user.getId())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(user.getId()))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.adminUser").value(true))
            .andExpect(jsonPath("$.data.user").value(true));

        assertEquals(UserPO.Status.ACTIVE, userRepository.findById(user.getId()).orElseThrow().getStatus());
    }

    @Test
    void adminCannotFreezeCurrentLoginUser() throws Exception {
        JsonNode adminData = adminLogin();
        String adminToken = adminData.path("accessToken").asText();
        long currentPrincipalUserId = adminData.path("principal").path("userId").asLong();

        mockMvc.perform(post("/api/platform/user/{id}/freeze", currentPrincipalUserId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("平台用户不能被冻结"));

        assertEquals(UserPO.Status.ACTIVE, userRepository.findById(currentPrincipalUserId).orElseThrow().getStatus());
    }

    @Test
    void adminCanFreezeNonAdminNameWhenIdentityFlagsDefaultTrue() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        UserPO user = new UserPO();
        user.setName("Freeze Candidate");
        user.setStatus(UserPO.Status.ACTIVE);
        user = userRepository.save(user);

        mockMvc.perform(post("/api/platform/user/{id}/freeze", user.getId())
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(user.getId()))
            .andExpect(jsonPath("$.data.status").value("FROZEN"))
            .andExpect(jsonPath("$.data.adminUser").value(true))
            .andExpect(jsonPath("$.data.user").value(true));

        assertEquals(UserPO.Status.FROZEN, userRepository.findById(user.getId()).orElseThrow().getStatus());
    }

    @Test
    void adminLoginMeAndLogoutWork() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        mockMvc.perform(get("/api/platform/user/me")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").isNumber())
            .andExpect(jsonPath("$.data.loginName").value(DEFAULT_ADMIN_PHONE));

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void adminEndpointsRejectMissingOrInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/password/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "phone", DEFAULT_ADMIN_PHONE,
                    "password", "wrong-password",
                    "clientType", "PC_WEB",
                    "appCode", "trade-leads-platform-web",
                    "accessScope", "PLATFORM"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("手机号或密码错误"));

        mockMvc.perform(get("/api/platform/user/page"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("未登录或 Token 无效"));
    }

    private JsonNode login(String code) throws Exception {
        String content = objectMapper.writeValueAsString(Map.of(
            "code", code,
            "phoneCode", phoneCodeFor(code),
            "clientType", "WECHAT_MINIAPP",
            "appCode", "trade-leads-miniapp",
            "accessScope", "CLIENT"
        ));
        String response = mockMvc.perform(post("/api/auth/wechat-miniapp/phone-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();
        ObjectNode data = (ObjectNode) objectMapper.readTree(response).path("data");
        AccountPO wechatAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("wechat_miniapp", "trade-leads-miniapp", "mock-openid-" + code)
            .orElseThrow();
        UserAccountPO link = userAccountRepository.findByAccountId(wechatAccount.getId()).orElseThrow();
        data.put("accountId", wechatAccount.getId());
        data.put("userId", link.getUserId());
        return data;
    }

    private String phoneCodeFor(String code) {
        int suffix = Math.floorMod(code.hashCode(), 100_000_000);
        return "138" + String.format("%08d", suffix);
    }

    private JsonNode createPaymentOrder(String accessToken, String bizType, long bizOrderId) throws Exception {
        String response = mockMvc.perform(post("/api/client/payment/orders")
                .header("Authorization", bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "bizType", bizType,
                    "bizOrderId", bizOrderId
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private JsonNode createOrder(String accessToken, String packageCode) throws Exception {
        String response = mockMvc.perform(post("/api/client/recharge/orders")
                .header("Authorization", bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("packageCode", packageCode))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private JsonNode adminLogin() throws Exception {
        String response = mockMvc.perform(post("/api/auth/password/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "phone", DEFAULT_ADMIN_PHONE,
                    "password", DEFAULT_ADMIN_PASSWORD,
                    "clientType", "PC_WEB",
                    "appCode", "trade-leads-platform-web",
                    "accessScope", "PLATFORM"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();
        ObjectNode data = (ObjectNode) objectMapper.readTree(response).path("data");
        AccountPO platformAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier(PLATFORM_PASSWORD_ACCOUNT_TYPE, "GLOBAL", DEFAULT_ADMIN_PHONE)
            .orElseThrow();
        UserAccountPO link = userAccountRepository.findByAccountId(platformAccount.getId()).orElseThrow();
        ObjectNode principal = objectMapper.createObjectNode();
        principal.put("userId", link.getUserId());
        principal.put("loginName", platformAccount.getIdentifier());
        data.set("principal", principal);
        return data;
    }

    private void seedDefaultAdmin() {
        UserPO adminUser = new UserPO();
        adminUser.setName("admin");
        adminUser.setPhone(DEFAULT_ADMIN_PHONE);
        adminUser.setStatus(UserPO.Status.ACTIVE);
        adminUser = userRepository.save(adminUser);

        bindAccount(adminUser.getId(), createAccount("phone", DEFAULT_ADMIN_PHONE, null));
        bindAccount(adminUser.getId(), createAccount("phone_password", DEFAULT_ADMIN_PHONE, passwordSecretCodec.encode(DEFAULT_ADMIN_PASSWORD)));
        bindAccount(adminUser.getId(), createAccount(PLATFORM_PASSWORD_ACCOUNT_TYPE, DEFAULT_ADMIN_PHONE, null));
    }

    private AccountPO createAccount(String type, String identifier, String secret) {
        AccountPO account = new AccountPO();
        account.setType(type);
        account.setCredentialScope(AccountPO.GLOBAL_CREDENTIAL_SCOPE);
        account.setIdentifier(identifier);
        account.setSecret(secret);
        return accountRepository.save(account);
    }

    private void bindAccount(Long userId, AccountPO account) {
        UserAccountPO link = new UserAccountPO();
        link.setUserId(userId);
        link.setAccountId(account.getId());
        userAccountRepository.save(link);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
