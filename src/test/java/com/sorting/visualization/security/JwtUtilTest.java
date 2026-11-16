package com.sorting.visualization.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** W1：JWT 签发与校验 */
class JwtUtilTest {

    private static final String SECRET = "sortviz-2026-secret-key-for-database-course-design";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 3600_000L);
    }

    @Test
    @DisplayName("签发的令牌可以被自己校验，载荷完整")
    void generateAndParse() {
        String token = jwtUtil.generateToken("u-1", "admin", "admin");

        assertThat(token).startsWith("eyJ");
        assertThat(token.split("\\.")).hasSize(3);

        Claims claims = jwtUtil.parse(token);
        assertThat(claims.getSubject()).isEqualTo("u-1");
        assertThat(claims.get(JwtUtil.CLAIM_USERNAME, String.class)).isEqualTo("admin");
        assertThat(claims.get(JwtUtil.CLAIM_ROLE, String.class)).isEqualTo("admin");
    }

    @Test
    @DisplayName("被篡改的令牌校验失败")
    void tamperedTokenRejected() {
        String token = jwtUtil.generateToken("u-1", "student", "student");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThatThrownBy(() -> jwtUtil.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("用别的密钥签发的令牌校验失败")
    void foreignSecretRejected() {
        JwtUtil other = new JwtUtil("another-secret-key-with-enough-length-1234", 3600_000L);
        String token = other.generateToken("u-2", "student", "student");

        assertThatThrownBy(() -> jwtUtil.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("过期的令牌校验失败")
    void expiredTokenRejected() {
        JwtUtil shortLived = new JwtUtil(SECRET, 1L);
        String token = shortLived.generateToken("u-3", "student", "student");

        assertThatThrownBy(() -> jwtUtil.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("手工构造的过期令牌不会被误判为有效")
    void manuallyExpiredTokenRejected() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("u-4")
                .issuedAt(new Date(System.currentTimeMillis() - 7200_000L))
                .expiration(new Date(System.currentTimeMillis() - 3600_000L))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtUtil.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("密钥过短时构造直接失败，避免弱密钥上线")
    void shortSecretRejected() {
        assertThatThrownBy(() -> new JwtUtil("too-short", 1000L))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtUtil(SECRET, 0L))
                .isInstanceOf(IllegalStateException.class);
    }
}
