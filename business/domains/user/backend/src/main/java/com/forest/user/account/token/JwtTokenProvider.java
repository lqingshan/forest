package com.forest.user.account.token;

import com.forest.starter.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * 生成、解析并刷新用户令牌。
 */
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:7200000}")
    private long expiration;

    @Value("${jwt.refresh-expiration:1209600000}")
    private long refreshExpiration;

    public String generateAccessToken(
        String sessionNo,
        String clientType,
        String appCode,
        String accessScope,
        String jti
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .setIssuer("forest")
            .setSubject(sessionNo)
            .claim("clientType", clientType)
            .claim("appCode", appCode)
            .claim("accessScope", accessScope)
            .claim("tokenType", "access")
            .setId(jti)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(
        String sessionNo,
        String clientType,
        String appCode,
        String accessScope,
        String jti
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
            .setIssuer("forest")
            .setSubject(sessionNo)
            .claim("clientType", clientType)
            .claim("appCode", appCode)
            .claim("accessScope", accessScope)
            .claim("tokenType", "refresh")
            .setId(jti)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact();
    }

    public String getSessionNoFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getClientTypeFromToken(String token) {
        return getClaims(token).get("clientType", String.class);
    }

    public String getAppCodeFromToken(String token) {
        return getClaims(token).get("appCode", String.class);
    }

    public String getAccessScopeFromToken(String token) {
        return getClaims(token).get("accessScope", String.class);
    }

    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getClaims(token).get("tokenType", String.class));
    }

    public String newTokenId() {
        return UUID.randomUUID().toString();
    }

    public long getAccessExpiresInSeconds() {
        return expiration / 1000L;
    }

    public long getRefreshExpiresInSeconds() {
        return refreshExpiration / 1000L;
    }

    public LocalDateTime getRefreshExpiresAtFromNow() {
        return new Date(System.currentTimeMillis() + refreshExpiration)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
