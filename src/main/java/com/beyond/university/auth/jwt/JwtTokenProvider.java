package com.beyond.university.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    private static final long ACCESS_TOKEN_EXP = 1000L * 60L * 15L; // 15분
    private static final long REFRESH_TOKEN_EXP = 1000L * 60L * 60L * 24; // 1일

    public JwtTokenProvider(
            @Value("${springboot.jwt.secret}") String secret,
            UserDetailsService userDetailsService,
            RedisTemplate<String, String> redisTemplate) {

        log.debug("Secret: {}", secret);

        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
    }

    // AccessToken을 생성하는 메소드
    public String createAccessToken(String username, String role) {
        Map<String, String> claims = new HashMap<>();

        claims.put("username", username);
        claims.put("role", role);

        return createToken(claims, ACCESS_TOKEN_EXP);
    }

    // RefreshToken을 생성하는 메소드
    public String createRefreshToken(String username) {
        Map<String, String> claims = Map.of("username", username);
        String refreshToken = createToken(claims, REFRESH_TOKEN_EXP);

        redisTemplate.opsForValue().set("refresh:" + username,
                refreshToken, REFRESH_TOKEN_EXP, TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    private String createToken(Map<String, String> claims, long tokenExp) {

        return Jwts.builder()
                .header().add("typ", "JWT").and() // typ 헤더 추가
                .claims(claims) // 공개 클래임
                .id(Long.toHexString(System.nanoTime())) // jti(JWT ID) 클래임
                .issuedAt(new Date()) // 발급 시간 설정
                .expiration(new Date(System.currentTimeMillis() + tokenExp)) // 만료 시간 설정
                .signWith(secretKey) // 서명을 생성
                .compact(); // JWT 토큰을 생성
    }

    // 토큰이 유효한지 체크하는 메소드 (토큰이 유효하면 true, 만료되었으면 false 반환)
    public boolean validateToken(String token) {

        return !getClaims(token).getExpiration().before(new Date());
    }

    // 클라이언트가 헤더를 통해 서버로 전달한 토큰을 추출하는 메소드
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    // SecurityContextHolder에 저장할 Authentication 객체를 생성하는 메소드
    public Authentication getAuthentication(String token) {
        String username = getUserName(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    public String getUserName(String token) {

        return getClaims(token).get("username").toString();
    }

    // 로그아웃 시 블랙리스트에 Access Token을 저장
    public void addBlacklist(String accessToken) {
        String key = "blacklist:" + getJti(accessToken);

        redisTemplate.opsForValue().set(key, "true", ACCESS_TOKEN_EXP, TimeUnit.MILLISECONDS);
    }

    private String getJti(String token) {

        return getClaims(token).getId();
    }

    // 블랙리스트 등록 여부 확인
    public boolean isBlacklisted(String token) {
        String key = "blacklist:" + getJti(token);

        return redisTemplate.hasKey(key);
    }

    // 권한 여부 확인
    public boolean hasRole(String token) {

        return getClaims(token).get("role") != null;
    }

    private Claims getClaims(String token) {
        // 토큰이 만료되면 parseSignedClaims() 단계에서 ExpiredJwtException이 발생한다.
        try {
            return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 리플레시 토큰 삭제
    public void deleteRefreshToken(String accessToken) {
        String username = getUserName(accessToken);

        redisTemplate.delete("refresh:" + username);
    }

    public boolean isValidRefreshToken(String refreshToken) {
        String username = getUserName(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + username);

        return storedRefreshToken != null && storedRefreshToken.equals(refreshToken);
    }
}