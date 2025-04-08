package com.beyond.university.auth.controller;

import com.beyond.university.auth.model.dto.LoginRequestDto;
import com.beyond.university.auth.model.dto.TokenResponseDto;
import com.beyond.university.auth.model.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth APIs", description = "인증 관련 API 목록")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 패스워드를 JSON으로 받아서 로그인한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL SERVER ERROR",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<TokenResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {

        TokenResponseDto tokenResponseDto = authService.login(
                loginRequestDto.getUsername(),
                loginRequestDto.getPassword()
        );

        return ResponseEntity.ok(tokenResponseDto);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Access Token을 전달받아 로그아웃한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "NO CONTENT",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL SERVER ERROR",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {

        authService.logout(bearerToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token 재발급")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL SERVER ERROR",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<TokenResponseDto> refresh(@RequestHeader("Authorization") String bearerToken) {

        TokenResponseDto tokenResponseDto = authService.refresh(bearerToken);

        return ResponseEntity.ok(tokenResponseDto);
    }
}
