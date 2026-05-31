package com.forest.tradeleads;

import com.forest.lead.entity.LeadPO;
import com.forest.lead.repository.LeadRepository;
import com.forest.lead.repository.LeadUnlockRecordRepository;
import com.forest.point.repository.PointBalanceRepository;
import com.forest.point.repository.PointLogRepository;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
/**
 * 基于 PostgreSQL 容器验证线索搜索的全文 + trigram 行为。
 */
class TradeLeadsSearchPostgresIntegrationTest {
    private static final String PLATFORM_PASSWORD_ACCOUNT_TYPE = "platform_password";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.3-alpine")
        .withDatabaseName("forest_search_test")
        .withUsername("forest")
        .withPassword("forest123");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "SET TIME ZONE 'Asia/Shanghai'");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("jwt.secret", () -> "test-secret-key-must-be-at-least-32-bytes-long");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    private PasswordSecretCodec passwordSecretCodec;

    private MockMvc mockMvc;
    private final JsonMapper objectMapper = JsonMapper.builder().build();
    private static final String DEFAULT_ADMIN_PHONE = "+8618257147892";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456abc";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        pointLogRepository.deleteAll();
        pointBalanceRepository.deleteAll();
        leadUnlockRecordRepository.deleteAll();
        leadRepository.deleteAll();
        userAccountRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        seedDefaultAdmin();
    }

    @Test
    void adminKeywordSearchUsesConfiguredFieldsAndExcludesDeletedRecords() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        LeadPO nameMatchedLead = saveLead("Forest Buyer", "Machinery", null, "CN", 0);
        LeadPO categoryMatchedLead = saveLead("Supplier Two", "Forest Equipment", null, "CN", 0);
        LeadPO keywordMatchedLead = saveLead("Supplier Three", "Lighting", "forest export", "US", 0);
        LeadPO deletedLead = saveLead("Forest Deleted", "Forest Hidden", null, "CN", 1);

        mockMvc.perform(get("/api/platform/lead/page")
                .header("Authorization", bearer(adminToken))
                .param("keyword", "Forest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.content[*].name", hasItems(
                nameMatchedLead.getName(),
                categoryMatchedLead.getName(),
                keywordMatchedLead.getName()
            )))
            .andExpect(jsonPath("$.data.content[*].name", not(hasItem(deletedLead.getName()))));
    }

    @Test
    void trigramFallbackMatchesPartialKeyword() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        saveLead("Supplier Alpha", "Lighting", null, "CN", 0);
        saveLead("Supplier Beta", "Machinery", null, "US", 0);
        saveLead("Buyer Hub", "Trading", null, "CN", 0);

        mockMvc.perform(get("/api/platform/lead/page")
                .header("Authorization", bearer(adminToken))
                .param("keyword", "suppl"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content[*].name", hasItems("Supplier Alpha", "Supplier Beta")));
    }

    @Test
    void adminAndClientSearchShareRankingAndRespectCountryFilter() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();
        String userToken = login("search_rank_case").path("accessToken").asText();

        LeadPO exactNameLead = saveLead("Forest Buyer", "Machinery", null, "CN", 0);
        LeadPO splitFieldLead = saveLead("Supplier Two", "Forest Equipment", "buyer export", "CN", 0);
        saveLead("Supplier Three", "Lighting", "forest export", "US", 0);

        JsonNode adminPage = performSearch("/api/platform/lead/page", adminToken, "forest buyer", "CN");
        JsonNode clientPage = performSearch("/api/client/user-lead/page", userToken, "forest buyer", "CN");

        assertEquals(
            List.of(exactNameLead.getId(), splitFieldLead.getId()),
            extractLeadIds(adminPage.path("content"))
        );
        assertEquals(
            extractLeadIds(adminPage.path("content")),
            extractLeadIds(clientPage.path("content"))
        );
    }

    @Test
    void updatedLeadBecomesSearchableImmediately() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();

        LeadPO lead = saveLead("Legacy Partner", "Furniture", "chairs", "CN", 0);

        mockMvc.perform(get("/api/platform/lead/page")
                .header("Authorization", bearer(adminToken))
                .param("keyword", "renamed"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(0));

        lead.setName("Renamed Supplier");
        leadRepository.save(lead);

        mockMvc.perform(get("/api/platform/lead/page")
                .header("Authorization", bearer(adminToken))
                .param("keyword", "renamed"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[*].name", contains("Renamed Supplier")));
    }

    @Test
    void punctuationOnlyKeywordReturnsEmptyPage() throws Exception {
        String adminToken = adminLogin().path("accessToken").asText();
        saveLead("Forest Buyer", "Machinery", null, "CN", 0);

        mockMvc.perform(get("/api/platform/lead/page")
                .header("Authorization", bearer(adminToken))
                .param("keyword", "  !!!   "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    private LeadPO saveLead(String name, String category, String keywords, String country, int deleted) {
        LeadPO lead = new LeadPO();
        lead.setName(name);
        lead.setCategory(category);
        lead.setKeywords(keywords);
        lead.setCountry(country);
        lead.setDeleted(deleted);
        return leadRepository.save(lead);
    }

    private JsonNode performSearch(String path, String accessToken, String keyword, String country) throws Exception {
        String response = mockMvc.perform(get(path)
                .header("Authorization", bearer(accessToken))
                .param("keyword", keyword)
                .param("country", country))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private List<Long> extractLeadIds(JsonNode content) {
        return java.util.stream.StreamSupport.stream(content.spliterator(), false)
            .map(item -> item.path("id").asLong())
            .toList();
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
        return objectMapper.readTree(response).path("data");
    }

    private String phoneCodeFor(String code) {
        int suffix = Math.floorMod(code.hashCode(), 100_000_000);
        return "138" + String.format("%08d", suffix);
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
        return objectMapper.readTree(response).path("data");
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
