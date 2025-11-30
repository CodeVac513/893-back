package com.samyookgoo.palgoosam.config;

import com.samyookgoo.palgoosam.auth.JwtTokenProvider;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.domain.UserJwtToken;
import com.samyookgoo.palgoosam.user.repository.UserJwtTokenRepository;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class  OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtProvider;
    private final UserJwtTokenRepository userJwtTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest req,
            HttpServletResponse res,
            Authentication auth
    ) throws IOException {
        // 1) JWT 생성
        String accessToken = jwtProvider.generateAccessToken(auth);
        String refreshToken = jwtProvider.generateRefreshToken(auth);

        // 2) JWT 토큰 디비에 저장

        // 2-1) 유저 id 찾기
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;

        String provider   = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = auth.getName();

        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new IllegalStateException("가입된 사용자가 아닙니다."));

        // 2-2) 업데이트 혹은 저장
        UserJwtToken userJwtToken = userJwtTokenRepository.findById(user.getId())
                .map(existing -> {
                    existing.setAuthToken(accessToken);
                    existing.setRefreshToken(refreshToken);
                    return existing;
                })
                .orElseGet(() ->
                        UserJwtToken.builder()
                                .user(user)
                                .authToken(accessToken)
                                .refreshToken(refreshToken)
                                .build()
                );

        userJwtTokenRepository.save(userJwtToken);

        // 3) HttpOnly 쿠키 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtProvider.getAccessValidityMs() / 1000)
                .sameSite("None")   // TODO 추후 "Lax"로 변경
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(jwtProvider.getRefreshValidityMs() / 1000)
                .sameSite("None")
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 4) 프론트로 리다이렉트
        String frontUrl = "http://localhost:3000";
        res.sendRedirect(frontUrl + "?loginSuccess");
    }
}