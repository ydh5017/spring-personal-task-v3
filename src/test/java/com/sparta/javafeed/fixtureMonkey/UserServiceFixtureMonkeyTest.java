package com.sparta.javafeed.fixtureMonkey;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.sparta.javafeed.dto.*;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.jwt.JwtUtil;
import com.sparta.javafeed.repository.ProfileRepository;
import com.sparta.javafeed.repository.UserRepository;
import com.sparta.javafeed.service.UserService;
import com.sparta.javafeed.util.S3Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceFixtureMonkeyTest {

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

    private FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Test
    void signupUser() {
        // given
        SignupRequestDto requestDto = fixtureMonkey.giveMeOne(SignupRequestDto.class);
        // when
        SignupResponseDto responseDto = userService.signupUser(requestDto);
        // then
        assertNotNull(responseDto);
    }

    @Test
    void deactiveUser() {
        //given
        PasswordReqeustDto reqeustDto = fixtureMonkey.giveMeOne(PasswordReqeustDto.class);
        String accountId = fixtureMonkey.giveMeOne(String.class);
        User user = new User();
        user.updateUserStatus(UserStatus.ACTIVE);

        given(userRepository.findByAccountId(any()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any()))
                .willReturn(true);
        // when
        userService.deactiveUser(reqeustDto, accountId);
        // then
        assertEquals(user.getUserStatus(), UserStatus.DEACTIVATE);
    }

    @Test
    void getUser() {
        //given
        String accountId = fixtureMonkey.giveMeOne(String.class);
        User user = new User();

        given(userRepository.findByAccountId(any()))
                .willReturn(Optional.of(user));
        // when
        UserInfoResponseDto responseDto = userService.getUser(accountId);
        // then
        assertNotNull(responseDto);
        assertEquals(user.getAccountId(), responseDto.getAccountId());
    }

    @Test
    void updateUser() {
        //given
        UserInfoRequestDto requestDto = fixtureMonkey.giveMeOne(UserInfoRequestDto.class);
        String accountId = fixtureMonkey.giveMeOne(String.class);
        User user = new User();

        given(userRepository.findByAccountId(any()))
                .willReturn(Optional.of(user));

        // when
        userService.updateUser(requestDto, accountId);

        // then
        assertEquals(user.getName(), requestDto.getName());
    }

    @Test
    void updatePassword() {
        //given
        PasswordUpdateDto requestDto = fixtureMonkey.giveMeOne(PasswordUpdateDto.class);
        String accountId = fixtureMonkey.giveMeOne(String.class);
        User user = new User();

        given(userRepository.findByAccountId(any()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any()))
                .willReturn(false, true);
        given(passwordEncoder.encode(any()))
                .willReturn(requestDto.getNewPassword());

        // when
        userService.updatePassword(requestDto, accountId);

        // then
        assertEquals(user.getPassword(), requestDto.getNewPassword());
    }

    @Test
    void logout() {
        //given
        String accessToken = fixtureMonkey.giveMeOne(String.class);
        String refreshToken = fixtureMonkey.giveMeOne(String.class);
        User user = new User();
        user.saveRefreshToken("refresh_token");

        given(userRepository.findByAccountId(any()))
                .willReturn(Optional.of(user));

        // when
        userService.logout(user, accessToken, refreshToken);

        // then
        assertTrue(user.getRefreshToken().isEmpty());
    }

    @Test
    void updateUserEmailSent() {
        //given
        String email = fixtureMonkey.giveMeOne(String.class);
        LocalDateTime sentAt = LocalDateTime.now();
        User user = new User();

        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(user));

        // when
        userService.updateUserEmailSent(email, sentAt);

        // then
        assertEquals(user.getEmailSentAt(), sentAt);
    }

    @Test
    void updateUserStatus() {
        //given
        EmailVerifyCheckRequestDto requestDto = fixtureMonkey.giveMeOne(EmailVerifyCheckRequestDto.class);
        User user = new User();
        user.updateUserStatus(UserStatus.DEACTIVATE);

        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(user));

        // when
        userService.updateUserStatus(requestDto);

        // then
        assertEquals(user.getUserStatus(), UserStatus.ACTIVE);
    }

    @Test
    void uploadProfile() {
        //given
        MultipartFile file = new MockMultipartFile(
                "file", "profile.jpg", "image/jpeg", "profile.jpg".getBytes());
        S3ResponseDto responseDto = fixtureMonkey.giveMeOne(S3ResponseDto.class);
        User user = new User();
        user.updateUserStatus(UserStatus.DEACTIVATE);

        given(s3Util.uploadFile(any(), any()))
                .willReturn(responseDto);
        given(userRepository.findByAccountId(any()))
                .willReturn(Optional.of(user));

        // when
        String url = userService.uploadProfile(file, user);

        // then
        assertNotNull(url);
        assertEquals(url, responseDto.getUrl());
    }
}
