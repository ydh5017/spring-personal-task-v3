package com.sparta.javafeed.service;

import com.sparta.javafeed.dto.*;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.jwt.JwtUtil;
import com.sparta.javafeed.repository.ProfileRepository;
import com.sparta.javafeed.repository.UserRepository;
import com.sparta.javafeed.util.S3Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Util s3Util;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    private User getUser() {
        SignupRequestDto requestDto =new SignupRequestDto(
                "user111111", "1q2w3e4r!@#$", "유동현", "ydh001027@gmail.com");

        String encodedPassword = encoder.encode(requestDto.getPassword());

        return new User(requestDto, encodedPassword);
    }

    @Test
    @DisplayName("회원 가입 성공")
    void signupUserSuccess() {
        // given
        SignupRequestDto requestDto =new SignupRequestDto(
                "user111111", "1q2w3e4r!@#$", "유동현", "ydh001027@gmail.com");

        String encodedPassword = encoder.encode(requestDto.getPassword());

        User user = getUser();

        given(passwordEncoder.encode(requestDto.getPassword())).willReturn(encodedPassword);

        // when
        SignupResponseDto responseDto = userService.signupUser(requestDto);

        // then
        assertNotNull(responseDto);
        assertEquals(responseDto.getPassword(), encodedPassword);
        assertEquals(responseDto.getAccountId(), user.getAccountId());
        assertEquals(responseDto.getEmail(), user.getEmail());
        assertEquals(responseDto.getName(), user.getName());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deactiveUserSuccess() {
        // given
        String password = "1q2w3e4r!@#$";
        User user = getUser();
        user.updateUserStatus(UserStatus.ACTIVE);

        PasswordReqeustDto passwordReqeust =new PasswordReqeustDto(password);

        given(userRepository.findByAccountId(user.getAccountId()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches(passwordReqeust.getPassword(), user.getPassword()))
                .willReturn(true);

        // when
        userService.deactiveUser(passwordReqeust, user.getAccountId());

        // then
        assertEquals(user.getUserStatus(), UserStatus.DEACTIVATE);
    }

    @Test
    @DisplayName("회원 상세정보 조회 성공")
    void getUserSuccess() {
        // given
        User user = getUser();

        given(userRepository.findByAccountId(user.getAccountId())).willReturn(Optional.of(user));

        // when
        UserInfoResponseDto response = userService.getUser(user.getAccountId());

        // then
        assertNotNull(response);
        assertEquals(response.getName(), user.getName());
        assertEquals(response.getEmail(), user.getEmail());
        assertEquals(response.getAccountId(), user.getAccountId());
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateUserSuccess() {
        // given
        User user = getUser();
        String name = "유동현2";
        String intro = "한 줄 소개";
        UserInfoRequestDto requestDto = new UserInfoRequestDto(name, intro);

        given(userRepository.findByAccountId(user.getAccountId())).willReturn(Optional.of(user));

        // when
        userService.updateUser(requestDto, user.getAccountId());

        // then
        assertEquals(name, user.getName());
        assertEquals(intro, user.getIntro());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePasswordSuccess() {
        // given
        User user = getUser();
        String currentPassword = "1q2w3e4r!@#$";
        String newPassword = "1q2w3e4r!@#$";
        PasswordUpdateDto passwordInfo = new PasswordUpdateDto(currentPassword, newPassword);

        String encodedPassword = encoder.encode(newPassword);

        given(userRepository.findByAccountId(user.getAccountId()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches(passwordInfo.getCurrentPassword(), user.getPassword()))
                .willReturn(false);
        given(passwordEncoder.encode(passwordInfo.getNewPassword()))
                .willReturn(encodedPassword);


        // when
        userService.updatePassword(passwordInfo, user.getAccountId());

        // then
        assertTrue(encoder.matches(newPassword, user.getPassword()));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void looutSuccess() {
        // given
        User user = getUser();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        user.saveRefreshToken(refreshToken);

        given(userRepository.findByAccountId(user.getAccountId()))
                .willReturn(Optional.of(user));

        // when
        userService.loout(user, accessToken, refreshToken);

        //then
        assertTrue(user.getRefreshToken().isEmpty());
    }

    @Test
    @DisplayName("이메일 인증코드 전송시간 저장 성공")
    void updateUserEmailSentSuccess() {
        // given
        User user = getUser();
        LocalDateTime sentAt = LocalDateTime.now();

        given(userRepository.findByEmail(user.getEmail()))
                .willReturn(Optional.of(user));

        // when
        userService.updateUserEmailSent(user.getEmail(), sentAt);

        // then
        assertEquals(user.getEmailSentAt(), sentAt);
    }

    @Test
    @DisplayName("이메일 인증 회원 상태 변경 성공")
    void updateUserStatusSuccess() {
        // given
        User user = getUser();
        EmailVerifyCheckRequestDto requestDto = new EmailVerifyCheckRequestDto(user.getEmail(), "20N63N7e");

        given(userRepository.findByEmail(user.getEmail()))
                .willReturn(Optional.of(user));

        // when
        userService.updateUserStatus(requestDto);

        // then
        assertEquals(user.getUserStatus(), UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("아이디로 회원 찾기 성공")
    void findByAccountIdSuccess() {
        // given
        User user = getUser();

        given(userRepository.findByAccountId(user.getAccountId()))
                .willReturn(Optional.of(user));

        // when
        User findUser = userService.findByAccountId(user.getAccountId());

        // then
        assertEquals(user, findUser);
    }

    @Test
    @DisplayName("이메일로 회원 찾기 성공")
    void findByEmailSuccess() {
        // given
        User user = getUser();

        given(userRepository.findByEmail(user.getEmail()))
                .willReturn(Optional.of(user));

        // when
        User findUser = userService.findByEmail(user.getEmail());

        // then
        assertEquals(user, findUser);
    }

    @Test
    @DisplayName("회원 프로필 이미지 업로드 성공")
    void uploadProfileSuccess() {
        // given
        User user = getUser();
        String url = "https://www.google.com";
        String saveName = "saveName.jpg";

        MultipartFile file = new MockMultipartFile(
                "file", "profile.jpg", "image/jpeg", "profile.jpg".getBytes());

        S3ResponseDto s3Response = new S3ResponseDto(
                file.getOriginalFilename(), saveName, url, 1000L);

        given(s3Util.uploadFile(file, "profile"))
                .willReturn(s3Response);
        given(userRepository.findByAccountId(user.getAccountId()))
                .willReturn(Optional.of(user));

        // when
        String getUrl = userService.uploadProfile(file, user);

        // then
        assertNotNull(url);
        assertEquals(url, getUrl);
        assertEquals(user.getProfile().getUrl(), url);
        assertEquals(user.getProfile().getSaveFileName(), saveName);
        assertEquals(user.getProfile().getUser(), user);
    }
}