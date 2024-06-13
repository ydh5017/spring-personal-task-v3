package com.sparta.javafeed.entity;

import com.sparta.javafeed.dto.S3ResponseDto;
import com.sparta.javafeed.dto.SignupRequestDto;
import com.sparta.javafeed.dto.UserInfoRequestDto;
import com.sparta.javafeed.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private User user;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUser() {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .accountId("user111111")
                .password("1q2w3e4r!@#$")
                .email("ydh001027@gmail.com")
                .name("유동현")
                .build();

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        user = new User(requestDto, encodedPassword);
    }

    @Test
    @DisplayName("UserStatus 변경 성공")
    void updateUserStatusSuccess() {
        // given
        UserStatus userStatus = UserStatus.ACTIVE;

        // when
        user.updateUserStatus(userStatus);

        //then
        assertEquals(userStatus, user.getUserStatus());
    }

    @Test
    @DisplayName("Refresh 토큰 저장 성공")
    void saveRefreshTokenSuccess() {
        // given
        String refreshToken = "refreshToken";

        // when
        user.saveRefreshToken(refreshToken);

        // then
        assertEquals(refreshToken, user.getRefreshToken());
    }

    @Test
    @DisplayName("Refresh 토큰 확인 성공")
    void checkRefreshTokenSuccess() {
        // given
        String refreshToken = "refreshToken";
        user.saveRefreshToken(refreshToken);

        // when
        boolean isValid = user.checkRefreshToken(refreshToken);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateUserInfoSuccess() {
        // given
        UserInfoRequestDto requestDto = UserInfoRequestDto.builder()
                .name("유동현2")
                .intro("한 줄 소개")
                .build();

        // when
        user.updateUserInfo(requestDto);

        // then
        assertEquals(requestDto.getName(), user.getName());
        assertEquals(requestDto.getIntro(), user.getIntro());
    }

    @Test
    @DisplayName("password 변경 성공")
    void updatePasswordSuccess() {
        // given
        String newPassword = "1q2w3e!@#$";
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // when
        user.updatePassword(encodedNewPassword);

        // then
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
    }

    @Test
    @DisplayName("회원 프로필 이미지 변경 성공")
    void updateProfileSuccess() {
        // given
        S3ResponseDto responseDto = new S3ResponseDto("originName", "saveName", "url", 100L);
        Profile profile = new Profile(responseDto, user);

        // when
        user.updateProfile(profile);

        // then
        assertEquals(user.getProfile().getUser(), profile.getUser());
        assertEquals(user.getProfile().getSaveFileName(), profile.getSaveFileName());
        assertEquals(user.getProfile().getUrl(), profile.getUrl());
    }
}