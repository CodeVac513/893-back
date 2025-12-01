package com.samyookgoo.palgoosam.config;

import com.samyookgoo.palgoosam.auth.JwtTokenProvider;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.domain.UserJwtToken;
import com.samyookgoo.palgoosam.user.exception.UserNotFoundException;
import com.samyookgoo.palgoosam.user.repository.UserJwtTokenRepository;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Transactional  // ⭐ 필수
public class TestTokenInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;
    private final UserJwtTokenRepository userJwtTokenRepository;  // ⭐ 추가

    @Override
    public void run(ApplicationArguments args) {
        User current = userRepository.findById(1L).orElseThrow(UserNotFoundException::new);

        String providerId = current.getProviderId();
        // Access Token과 Refresh Token 둘 다 생성
        String accessToken = jwtProvider.generateNeverExpireToken(providerId);
        String refreshToken = jwtProvider.generateNeverExpireRefreshToken(providerId); // ⭐ 추가 필요

        // user_jwt_token 테이블에 저장 (기존 데이터가 있으면 업데이트)
        UserJwtToken userJwtToken = userJwtTokenRepository.findById(current.getId())
                .map(existing -> {
                    existing.setAuthToken(accessToken);
                    existing.setRefreshToken(refreshToken);
                    return existing;
                })
                .orElseGet(() -> {
                            UserJwtToken newToken = new UserJwtToken();
                            newToken.setUser(current);  // ⭐ @Transactional 안에서는 안전
                            newToken.setAuthToken(accessToken);
                            newToken.setRefreshToken(accessToken);
                            return newToken;
                        }
                );

        userJwtTokenRepository.save(userJwtToken);
    }
}