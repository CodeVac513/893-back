package com.samyookgoo.palgoosam.user.controller;

import com.samyookgoo.palgoosam.auth.JwtTokenProvider;
import com.samyookgoo.palgoosam.auth.service.AuthService;
import com.samyookgoo.palgoosam.common.response.BaseResponse;
import com.samyookgoo.palgoosam.user.api_docs.GetUserAuctionsApi;
import com.samyookgoo.palgoosam.user.api_docs.GetUserBidsApi;
import com.samyookgoo.palgoosam.user.api_docs.GetUserInfoApi;
import com.samyookgoo.palgoosam.user.api_docs.GetUserPaymentsApi;
import com.samyookgoo.palgoosam.user.api_docs.GetUserScrapsApi;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.dto.UserAuctionsResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserBidsResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserInfoResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserPaymentsResponseDto;
import com.samyookgoo.palgoosam.user.exception.UserNotFoundException;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import com.samyookgoo.palgoosam.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 정보 조회 API")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;

    @GetUserInfoApi
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/user-info")
    public ResponseEntity<BaseResponse<UserInfoResponseDto>> getUserInfo() {
        User currentUser = authService.getAuthorizedUser(authService.getCurrentUser());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("사용자 정보가 성공적으로 조회됐습니다.", userService.getUserInfo(currentUser)));
    }

    @GetUserBidsApi
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/bids")
    public ResponseEntity<BaseResponse<List<UserBidsResponseDto>>> getUserBids() {
        User currentUser = authService.getAuthorizedUser(authService.getCurrentUser());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("내 입찰이 성공적으로 조회됐습니다.", userService.getUserBids(currentUser)));
    }

    @GetUserAuctionsApi
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/auctions")
    public ResponseEntity<BaseResponse<List<UserAuctionsResponseDto>>> getUserAuctions() {
        User currentUser = authService.getAuthorizedUser(authService.getCurrentUser());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("내 경매가 성공적으로 조회됐습니다.", userService.getUserAuctions(currentUser)));
    }

    @GetUserScrapsApi
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/scraps")
    public ResponseEntity<BaseResponse<List<UserAuctionsResponseDto>>> getUserScraps() {
        User currentUser = authService.getAuthorizedUser(authService.getCurrentUser());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("스크랩 경매가 성공적으로 조회됐습니다.", userService.getUserScraps(currentUser)));
    }

    @GetUserPaymentsApi
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/payments")
    public ResponseEntity<BaseResponse<List<UserPaymentsResponseDto>>> getUserPayments() {
        User currentUser = authService.getAuthorizedUser(authService.getCurrentUser());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("내 결제 내역이 성공적으로 조회됐습니다.", userService.getUserPayments(currentUser)));
    }

    @PostMapping("/test/token/{userId}")
    public ResponseEntity<BaseResponse<String>> generateTestToken(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId().toString(),  // principal
                null,                     // credentials (비밀번호 불필요)
                Collections.emptyList()   // authorities
        );
        // JwtTokenProvider가 User 받는 메서드 있는지 확인
        String token = jwtProvider.generateAccessToken(authentication);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("JWT 토큰 반환.", token));
    }
}
