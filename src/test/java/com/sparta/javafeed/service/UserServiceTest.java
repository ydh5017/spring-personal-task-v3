package com.sparta.javafeed.service;

import com.sparta.javafeed.dto.*;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.ErrorType;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.exception.CustomException;
import com.sparta.javafeed.jwt.JwtUtil;
import com.sparta.javafeed.repository.ProfileRepository;
import com.sparta.javafeed.repository.UserRepository;
import com.sparta.javafeed.util.S3Util;
import org.junit.jupiter.api.*;
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

    @Nested
    @DisplayName("회원 가입")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class signupUser {

        @Test
        @Order(1)
        @DisplayName("회원 가입 - 성공")
        void when_Signup_expect_Success() {
            // given
            SignupRequestDto requestDto =new SignupRequestDto(
                    "user111111", "1q2w3e4r!@#$", "유동현", "ydh001027@gmail.com");

            String encodedPassword = encoder.encode(requestDto.getPassword());

            User user = getUser();

            given(passwordEncoder.encode(requestDto.getPassword()))
                    .willReturn(encodedPassword);

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
        @Order(2)
        @DisplayName("회원 가입 - 아이디 중복 예외")
        void when_Signup_expect_IdIsDuplicated() {
            // given
            SignupRequestDto requestDto =new SignupRequestDto(
                    "user111111", "1q2w3e4r!@#$", "유동현", "ydh001027@gmail.com");

            User user = getUser();

            given(userRepository.findByAccountId(requestDto.getAccountId()))
                    .willReturn(Optional.of(user));

            // when, then
            CustomException exception = assertThrows(CustomException.class, () -> userService.signupUser(requestDto));
            assertEquals(exception.getErrorType(), ErrorType.DUPLICATE_ACCOUNT_ID);
        }

        @Test
        @Order(3)
        @DisplayName("회원 가입 - 이메일 중복 예외")
        void when_Signup_expect_EmailIsDuplicated() {
            // given
            SignupRequestDto requestDto =new SignupRequestDto(
                    "user111111", "1q2w3e4r!@#$", "유동현", "ydh001027@gmail.com");

            User user = getUser();

            given(userRepository.findByEmail(requestDto.getEmail()))
                    .willReturn(Optional.of(user));

            // when, then
            CustomException exception = assertThrows(CustomException.class, () -> userService.signupUser(requestDto));
            assertEquals(exception.getErrorType(), ErrorType.DUPLICATE_EMAIL);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class deactiveUser {

        @Test
        @Order(1)
        @DisplayName("회원 탈퇴 - 성공")
        void when_UserDeactive_expect_Success() {
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
        @Order(2)
        @DisplayName("회원 탈퇴 - 탈퇴한 회원일 경우")
        void when_AlreadyDeactive_expect_ThrowException() {
            // given
            String password = "1q2w3e4r!@#$";
            User user = getUser();
            user.updateUserStatus(UserStatus.DEACTIVATE);

            PasswordReqeustDto passwordReqeust =new PasswordReqeustDto(password);

            given(userRepository.findByAccountId(user.getAccountId()))
                    .willReturn(Optional.of(user));

            // when, then
            CustomException exception = assertThrows(
                    CustomException.class, () -> userService.deactiveUser(passwordReqeust, user.getAccountId()));
            assertEquals(exception.getErrorType(), ErrorType.DEACTIVATE_USER);
        }

        @Test
        @Order(3)
        @DisplayName("회원 탈퇴 - 비밀번호가 유효하지 않을 경우")
        void when_PasswordIsInvalid_expect_ThrowException() {
            // given
            String password = "1q2w3e4r!@#$";
            User user = getUser();
            user.updateUserStatus(UserStatus.ACTIVE);

            PasswordReqeustDto passwordReqeust =new PasswordReqeustDto(password);

            given(userRepository.findByAccountId(user.getAccountId()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(passwordReqeust.getPassword(), user.getPassword()))
                    .willReturn(false);

            // when, then
            CustomException exception = assertThrows(
                    CustomException.class, () -> userService.deactiveUser(passwordReqeust, user.getAccountId()));
            assertEquals(exception.getErrorType(), ErrorType.INVALID_PASSWORD);
        }
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

    @Nested
    @DisplayName("비밀번호 변경")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class updatePassword {

        @Test
        @Order(1)
        @DisplayName("비밀번호 변경 - 성공")
        void when_UpdatePassword_expect_Success() {
            // given
            User user = getUser();
            String currentPassword = "1q2w3e4r!@#$";
            String newPassword = "1q2w3e4r!@#$";
            PasswordUpdateDto passwordInfo = new PasswordUpdateDto(currentPassword, newPassword);

            String encodedPassword = encoder.encode(newPassword);

            given(userRepository.findByAccountId(user.getAccountId()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(passwordInfo.getCurrentPassword(), user.getPassword()))
                    .willReturn(false, true);
            given(passwordEncoder.encode(passwordInfo.getNewPassword()))
                    .willReturn(encodedPassword);


            // when
            userService.updatePassword(passwordInfo, user.getAccountId());

            // then
            assertTrue(encoder.matches(newPassword, user.getPassword()));
        }

        @Test
        @Order(2)
        @DisplayName("비밀번호 변경 - 현재, 새로운 비밀번호가 중복될 경우")
        void when_CurrentAndNewPasswordIsDuplicated_expect_ThrowException() {
            // given
            User user = getUser();
            String currentPassword = "1q2w3e4r!@#$";
            String newPassword = "1q2w3e4r!@#$";
            PasswordUpdateDto passwordInfo = new PasswordUpdateDto(currentPassword, newPassword);

            String encodedPassword = encoder.encode(newPassword);

            given(userRepository.findByAccountId(user.getAccountId()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(passwordInfo.getCurrentPassword(), user.getPassword()))
                    .willReturn(true, false);

            // when
            CustomException exception = assertThrows(
                    CustomException.class, () ->userService.updatePassword(passwordInfo, user.getAccountId()));

            // then
            assertEquals(exception.getErrorType(), ErrorType.DUPLICATE_PASSWORD);
        }

        @Test
        @Order(3)
        @DisplayName("비밀번호 변경 - 현재 비밀번호가 다를 경우")
        void when_CurrentPasswordIsInvalid_expect_ThrowException() {
            // given
            User user = getUser();
            String currentPassword = "1q2w3e4r!@#$";
            String newPassword = "1q2w3e4r!@#$";
            PasswordUpdateDto passwordInfo = new PasswordUpdateDto(currentPassword, newPassword);

            String encodedPassword = encoder.encode(newPassword);

            given(userRepository.findByAccountId(user.getAccountId()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(passwordInfo.getCurrentPassword(), user.getPassword()))
                    .willReturn(false, false);

            // when
            CustomException exception = assertThrows(
                    CustomException.class, () ->userService.updatePassword(passwordInfo, user.getAccountId()));

            // then
            assertEquals(exception.getErrorType(), ErrorType.INVALID_PASSWORD);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class logout {

        @Test
        @Order(1)
        @DisplayName("로그아웃 - 성공")
        void when_Logout_expect_Success() {
            // given
            User user = getUser();
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";
            user.saveRefreshToken(refreshToken);

            given(userRepository.findByAccountId(user.getAccountId()))
                    .willReturn(Optional.of(user));

            // when
            userService.logout(user, accessToken, refreshToken);

            //then
            assertTrue(user.getRefreshToken().isEmpty());
        }

        @Test
        @Order(2)
        @DisplayName("로그아웃 - user 매개변수가 null인 경우")
        void when_UserIsNull_expect_ThrowException() {
            // given
            User user = getUser();
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";
            user.saveRefreshToken(refreshToken);

            // when
            CustomException exception = assertThrows(
                    CustomException.class, () -> userService.logout(null, accessToken, refreshToken));

            //then
            assertEquals(exception.getErrorType(), ErrorType.LOGGED_OUT_TOKEN);
        }

        @Test
        @Order(3)
        @DisplayName("로그아웃 - 회원이 존재하지 않는 경우")
        void when_UserNotExist_expect_ThrowException() {
            // given
            User user = getUser();
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";
            user.saveRefreshToken(refreshToken);

            // when
            CustomException exception = assertThrows(
                    CustomException.class, () -> userService.logout(user, accessToken, refreshToken));

            //then
            assertEquals(exception.getErrorType(), ErrorType.NOT_FOUND_USER);
        }
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

    @Nested
    @DisplayName("회원 찾기")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findUser {

        @Test
        @Order(1)
        @DisplayName("회원 찾기 - 아이디")
        void when_FindUserByAccountId_expect_Success() {
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
        @Order(2)
        @DisplayName("회원 찾기 - 이메일")
        void when_FindUserByEmail_expect_Success() {
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
        @Order(3)
        @DisplayName("회원 찾기 실패 - 아이디")
        void when_FindUserByAccountId_expect_Fail() {
            // given
            User user = getUser();

            // when
            CustomException exception = assertThrows(
                    CustomException.class, () -> userService.findByAccountId(user.getAccountId()));

            // then
            assertEquals(exception.getErrorType(), ErrorType.INVALID_ACCOUNT_ID);
        }

        @Test
        @Order(4)
        @DisplayName("회원 찾기 실패 - 이메일")
        void when_FindUserByEmail_expect_Fail() {
            // given
            User user = getUser();

            // when
            CustomException exception = assertThrows(
                    CustomException.class, () -> userService.findByEmail(user.getEmail()));

            // then
            assertEquals(exception.getErrorType(), ErrorType.INVALID_EMAIL);
        }
    }

    @Nested
    @DisplayName("회원 프로필 이미지 업로드")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class uploadProfile {

        @Test
        @Order(1)
        @DisplayName("회원 프로필 이미지 업로드 - 성공")
        void when_UploadProfile_expect_Success() {
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

        @Test
        @Order(2)
        @DisplayName("회원 프로필 이미지 업로드 - 실패")
        void when_UploadProfile_expect_Fail() {
            // given
            User user = getUser();

            MultipartFile file = new MockMultipartFile(
                    "null", "", "", "".getBytes());

            // when
            CustomException exception = assertThrows(
                    CustomException.class, () -> userService.uploadProfile(file, user));

            // then
            assertEquals(exception.getErrorType(), ErrorType.DOES_NOT_EXIST_FILE);
        }
    }
}