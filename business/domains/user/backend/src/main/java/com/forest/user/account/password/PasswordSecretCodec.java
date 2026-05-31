package com.forest.user.account.password;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 编码并校验密码密文。
 */
@Component
public class PasswordSecretCodec {
    private static final String SHA256_PREFIX = "sha256$";

    public String encode(String rawPassword) {
        return SHA256_PREFIX + sha256(rawPassword == null ? "" : rawPassword);
    }

    public boolean matches(String rawPassword, String storedSecret) {
        if (storedSecret == null) {
            return false;
        }
        if (storedSecret.startsWith(SHA256_PREFIX)) {
            return encode(rawPassword).equals(storedSecret);
        }
        // 兼容历史开发库中的明文 admin 密码；新增和迁移后的密码不再使用该形态。
        return storedSecret.equals(rawPassword);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", ex);
        }
    }
}
