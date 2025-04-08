package com.beyond.university.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. HttpServletRequest에서 토큰을 추출
        String token = jwtTokenProvider.resolveToken(request.getHeader("Authorization"));

        // 2. 추출한 토큰의 유효성을 검사 & 블랙리스트 확인 & Access Token 확인
        if (isUsableAccessToken(token)) {
            // 3. Authentication 객체를 생성 후 SecurityContext에 저장
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isUsableAccessToken(String token) {
        return token != null
                && jwtTokenProvider.validateToken(token)
                && !jwtTokenProvider.isBlacklisted(token)
                && jwtTokenProvider.hasRole(token);
    }
}
