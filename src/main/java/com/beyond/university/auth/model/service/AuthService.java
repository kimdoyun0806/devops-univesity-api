package com.beyond.university.auth.model.service;

import com.beyond.university.auth.model.dto.TokenResponseDto;

public interface AuthService {
    TokenResponseDto login(String username, String password);

    void logout(String bearerToken);

    TokenResponseDto refresh(String bearerToken);
}
