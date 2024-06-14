package com.sparta.javafeed.service;

import com.sparta.javafeed.dto.*;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.jwt.JwtUtil;
import com.sparta.javafeed.repository.ProfileRepository;
import com.sparta.javafeed.repository.UserRepository;
import com.sparta.javafeed.util.S3Util;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Util s3Util;

    @Autowired
    private UserService userService;

    private User user;


    @Test
    @Order(1)
    @DisplayName("회원가입")
    void signupUser() {
        // given
        SignupRequestDto requestDto =new SignupRequestDto(
                "test111111", "1q2w3e4r!@#$", "test", "test@gmail.com");

        // when
        SignupResponseDto responseDto = userService.signupUser(requestDto);

        // then
        assertNotNull(responseDto);
        assertEquals(responseDto.getAccountId(), requestDto.getAccountId());
        assertEquals(responseDto.getEmail(), requestDto.getEmail());
        assertEquals(responseDto.getName(), requestDto.getName());
    }

    @Test
    @Order(2)
    @DisplayName("AccountId로 회원 찾기")
    void findByAccountId() {
        // given
        String accountId = "test111111";

        // when
        user = userService.findByAccountId(accountId);

        assertNotNull(user);
        assertEquals(user.getAccountId(), accountId);
    }

    @Test
    @Order(3)
    @DisplayName("회원 상세정보 조회")
    void getUser() {
        // given
        // when
        UserInfoResponseDto response = userService.getUser(user.getAccountId());

        // then
        assertNotNull(response);
        assertEquals(response.getEmail(), user.getEmail());
        assertEquals(response.getName(), user.getName());
    }

    @Test
    @Order(4)
    @DisplayName("회원 정보 수정")
    void updateUser() {
        // given
        UserInfoRequestDto request = new UserInfoRequestDto("updateName", "한 줄 소개");

        // when
        userService.updateUser(request, user.getAccountId());

        // then
        UserInfoResponseDto response = userService.getUser(user.getAccountId());
        assertNotNull(response);
        assertEquals(response.getName(), request.getName());
        assertEquals(response.getIntro(), request.getIntro());
    }

    @Test
    @Order(5)
    @DisplayName("비밀번호 변경")
    void updatePassword() {
        // given
        PasswordUpdateDto request = new PasswordUpdateDto("1q2w3e4r!@#$", "1qaz2wsx3edc!@#");

        // when
        userService.updatePassword(request, user.getAccountId());

        // then
        user = userService.findByAccountId(user.getAccountId());
        assertNotNull(user);
        assertTrue(passwordEncoder.matches(request.getNewPassword(), user.getPassword()));
    }

    @Test
    @Order(6)
    @DisplayName("이메일 인증코드 전송")
    void updateUserEmailSent() {
        // given
        LocalDateTime sentAt = LocalDateTime.now();

        // when
        userService.updateUserEmailSent(user.getEmail(), sentAt);

        // then
        user = userService.findByEmail(user.getEmail());
        assertNotNull(user);
        assertEquals(user.getEmailSentAt(), sentAt);
    }

    @Test
    @Order(7)
    @DisplayName("이메일 인증 회원 상태 변경")
    void updateUserStatus() {
        // given
        EmailVerifyCheckRequestDto request = new EmailVerifyCheckRequestDto(user.getEmail(), "authNum");

        // when
        userService.updateUserStatus(request);

        // then
        user = userService.findByEmail(user.getEmail());
        assertNotNull(user);
        assertEquals(user.getUserStatus(), UserStatus.ACTIVE);
    }

    @Test
    @Order(8)
    @DisplayName("email로 회원 찾기")
    void findByEmail() {
        // given
        User user2 = user;
        // when
        user = userService.findByEmail(user.getEmail());

        // then
        assertNotNull(user);
        assertEquals(user.getEmail(), user2.getEmail());
        assertEquals(user.getName(), user2.getName());
    }

    @Test
    @Order(9)
    @DisplayName("로그아웃")
    void loout() {
        // given
        user.saveRefreshToken("refreshToken");

        // when
        userService.loout(user, "accessToken", "refreshToken");

        // then
        user = userService.findByEmail(user.getEmail());
        assertNotNull(user);
        assertTrue(user.getRefreshToken().isEmpty());
    }

    @Test
    void deactiveUser() {
        // given
        PasswordReqeustDto reqeust = new PasswordReqeustDto("1qaz2wsx3edc!@#");

        // when
        userService.deactiveUser(reqeust, user.getAccountId());

        // then
        user = userService.findByEmail(user.getEmail());
        assertNotNull(user);
        assertEquals(UserStatus.DEACTIVATE, user.getUserStatus());
    }
}