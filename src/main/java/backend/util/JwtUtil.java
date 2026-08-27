package backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String email) {
        return buildToken(email, TYPE_ACCESS, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, TYPE_REFRESH, refreshTokenExpirationMs);
    }

    private String buildToken(String email, String type, long expirationMs) {
        return Jwts.builder()
                .subject(email)
                .claim(TYPE_CLAIM, type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Access token 검증: 서명/만료가 유효하고 type이 "access"인 경우에만 true
    public boolean validateAccessToken(String token) {
        return validateTokenOfType(token, TYPE_ACCESS);
    }

    // Refresh token 검증: 서명/만료가 유효하고 type이 "refresh"인 경우에만 true
    public boolean validateRefreshToken(String token) {
        return validateTokenOfType(token, TYPE_REFRESH);
    }

    private boolean validateTokenOfType(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return expectedType.equals(claims.get(TYPE_CLAIM, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
