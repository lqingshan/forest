package com.forest.tradeleads;

import com.forest.lead.platform.controller.LeadPlatformController;
import com.forest.payment.client.controller.PaymentClientController;
import com.forest.payment.service.impl.PaymentOrderServiceImpl;
import com.forest.point.client.controller.PointClientController;
import com.forest.recharge.client.controller.RechargeClientController;
import com.forest.recharge.service.impl.RechargeOrderServiceImpl;
import com.forest.starter.auth.TokenAuthInterceptor;
import com.forest.user.user.platform.controller.UserPlatformController;
import com.forest.user.user.client.controller.UserClientController;
import com.forest.userlead.client.controller.UserLeadClientController;
import com.forest.userpoint.platform.controller.UserPointPlatformController;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 约束应用层与控制器层不得直接依赖实现类。
 */
class ServiceInterfaceArchitectureTest {
    @Test
    void controllersAndAppServicesMustNotDependOnImplClasses() {
        assertNoImplDependency(RechargeClientController.class);
        assertNoImplDependency(PaymentClientController.class);
        assertNoImplDependency(TokenAuthInterceptor.class);
        assertNoImplDependency(UserPlatformController.class);
        assertNoImplDependency(UserClientController.class);
        assertNoImplDependency(UserLeadClientController.class);
        assertNoImplDependency(LeadPlatformController.class);
        assertNoImplDependency(PointClientController.class);
        assertNoImplDependency(UserPointPlatformController.class);
        assertNoImplDependency(RechargeOrderServiceImpl.class);
        assertNoImplDependency(PaymentOrderServiceImpl.class);
    }

    @Test
    void businessEventContractsMustNotCreateDomainCycles() throws IOException {
        Path root = Path.of(System.getProperty("user.dir")).resolve("../../..").normalize();
        Path pointsPom = root.resolve("business/domains/point/backend/pom.xml");
        Path pointsSource = root.resolve("business/domains/point/backend/src/main/java");
        Path leadsPom = root.resolve("business/domains/lead/backend/pom.xml");
        Path leadsSource = root.resolve("business/domains/lead/backend/src/main/java");
        Path userPom = root.resolve("business/domains/user/backend/pom.xml");
        Path userSource = root.resolve("business/domains/user/backend/src/main/java");
        Path commonPom = root.resolve("business/common/backend/pom.xml");
        Path commonSource = root.resolve("business/common/backend/src/main/java");
        Path starterAuthPom = root.resolve("base-backend/starter-auth/pom.xml");
        Path starterAuthSource = root.resolve("base-backend/starter-auth/src/main/java");

        String pointsPomContent = Files.readString(pointsPom);
        assertFalse(pointsPomContent.contains("<artifactId>forest-business-user</artifactId>"),
            "points 模块不应直接依赖 business-user");
        assertFalse(pointsPomContent.contains("<artifactId>forest-aggregation-user-point</artifactId>"),
            "point 模块不应依赖 user-point aggregation");
        assertFalse(pointsPomContent.contains("<artifactId>forest-aggregation-user-lead</artifactId>"),
            "point 模块不应依赖 user-lead aggregation");
        assertSourceDoesNotContain(pointsSource, "com.forest.user.");
        assertSourceDoesNotContain(pointsSource, "com.forest.userpoint.");
        assertSourceDoesNotContain(pointsSource, "com.forest.userlead.");

        String leadsPomContent = Files.readString(leadsPom);
        assertFalse(leadsPomContent.contains("<artifactId>forest-business-user</artifactId>"),
            "lead 模块不应直接依赖 business-user");
        assertFalse(leadsPomContent.contains("<artifactId>forest-business-point</artifactId>"),
            "lead 模块不应直接依赖 business-point");
        assertFalse(leadsPomContent.contains("<artifactId>forest-aggregation-user-point</artifactId>"),
            "lead 模块不应依赖 user-point aggregation");
        assertFalse(leadsPomContent.contains("<artifactId>forest-aggregation-user-lead</artifactId>"),
            "lead 模块不应依赖 user-lead aggregation");
        assertSourceDoesNotContain(leadsSource, "com.forest.user.");
        assertSourceDoesNotContain(leadsSource, "com.forest.point.");
        assertSourceDoesNotContain(leadsSource, "com.forest.userpoint.");
        assertSourceDoesNotContain(leadsSource, "com.forest.userlead.");

        String userPomContent = Files.readString(userPom);
        assertFalse(userPomContent.contains("<artifactId>forest-aggregation-user-point</artifactId>"),
            "user 模块不应依赖 user-point aggregation");
        assertFalse(userPomContent.contains("<artifactId>forest-aggregation-user-lead</artifactId>"),
            "user 模块不应依赖 user-lead aggregation");
        assertSourceDoesNotContain(userSource, "com.forest.userpoint.");
        assertSourceDoesNotContain(userSource, "com.forest.userlead.");

        String commonPomContent = Files.readString(commonPom);
        assertFalse(commonPomContent.contains("<artifactId>forest-business-user</artifactId>"),
            "business-common 不应依赖 business-user");
        assertFalse(commonPomContent.contains("<artifactId>forest-business-point</artifactId>"),
            "business-common 不应依赖 business-point");
        assertFalse(commonPomContent.contains("<artifactId>forest-business-lead</artifactId>"),
            "business-common 不应依赖 business-lead");
        assertSourceDoesNotContain(commonSource, "com.forest.user.");
        assertSourceDoesNotContain(commonSource, "com.forest.point.");
        assertSourceDoesNotContain(commonSource, "com.forest.lead.");

        String starterAuthPomContent = Files.readString(starterAuthPom);
        assertFalse(starterAuthPomContent.contains("<artifactId>forest-business-user</artifactId>"),
            "starter-auth 不应依赖 business-user");
        assertSourceDoesNotContain(starterAuthSource, "com.forest.user.");
    }

    private void assertNoImplDependency(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (isImplType(field.getType())) {
                fail(type.getName() + " 不应直接依赖实现类字段: " + field.getType().getName());
            }
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                if (isImplType(parameterType)) {
                    fail(type.getName() + " 不应直接依赖实现类构造参数: " + parameterType.getName());
                }
            }
        }
    }

    private boolean isImplType(Class<?> type) {
        return type.getName().contains(".impl.");
    }

    private void assertSourceDoesNotContain(Path sourceRoot, String forbiddenText) throws IOException {
        try (var files = Files.walk(sourceRoot)) {
            boolean found = files
                .filter(path -> path.toString().endsWith(".java"))
                .anyMatch(path -> contains(path, forbiddenText));
            assertFalse(found, sourceRoot + " 不应包含 " + forbiddenText);
        }
    }

    private boolean contains(Path path, String text) {
        try {
            return Files.readString(path).contains(text);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
