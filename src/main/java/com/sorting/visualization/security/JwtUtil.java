package com.sorting.visualization.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与校验（HS256）。
 *
 * <p>密钥与有效期来自 {@code application.properties} 的 {@code jwt.secret} / {@code jwt.expiration}，
 * 这两项配置在 2026-05-24 的「数据库集成」提交里就已经写好，但一直没有签发与校验的代码，
 * 本次收尾补齐（见 WBS 的 W1「补完未竟设计」）。</p>
 */
public class JwtUtil {

    /** 载荷：用户名 */
    public static final String CLAIM_USERNAME = "username";
    /** 载荷：角色（student / teacher / admin） */
    public static final String CLAIM_ROLE = "role";

    /** HS256 要求密钥不短于 256 bit，即 32 字节 */
    static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationMillis;

    /** 由 {@code JwtConfig} 用配置项构造；单元测试也可直接 new */
    public JwtUtil(String secret, long expirationMillis) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("jwt.secret 至少需要 " + MIN_SECRET_BYTES + " 字节");
        }
        if (expirationMillis <= 0) {
            throw new IllegalStateException("jwt.expiration 必须大于 0");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /** 签发令牌：subject 为用户主键，附带用户名与角色 */
    public String generateToken(String userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 校验并解析令牌。
     *
     * @throws JwtException 签名不符、格式错误或已过期
     */
    public Claims parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }
}
