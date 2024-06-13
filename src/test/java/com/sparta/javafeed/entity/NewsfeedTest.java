package com.sparta.javafeed.entity;

import com.sparta.javafeed.dto.NewsfeedRequestDto;
import com.sparta.javafeed.dto.SignupRequestDto;
import com.sparta.javafeed.dto.UserInfoRequestDto;
import com.sparta.javafeed.enums.ErrorType;
import com.sparta.javafeed.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class NewsfeedTest {

    private Newsfeed newsfeed;

    @BeforeEach
    void setUp() {
        User user = setUser();
        newsfeed = new Newsfeed("title", "description", user);
    }

    private User setUser() {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .accountId("user111111")
                .password("1q2w3e4r!@#$")
                .email("ydh001027@gmail.com")
                .name("유동현")
                .build();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        return new User(requestDto, encodedPassword);
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateSuccess() {
        // given
        NewsfeedRequestDto requestDto = new NewsfeedRequestDto("updateTitle", "updateDescription", null);

        // when
        newsfeed.update(requestDto);

        //then
        assertEquals(newsfeed.getTitle(), requestDto.getTitle());
        assertEquals(newsfeed.getDescription(), requestDto.getDescription());
    }

    @Test
    @DisplayName("사용자 검증 성공")
    void userValidateSuccess() {
        // given
        User user = setUser();

        //when, then
        newsfeed.userValidate(user);
    }

    @Test
    @DisplayName("사용자 검증 실패")
    void userValidateFail() {
        // given
        User user = setUser();

        UserInfoRequestDto requestDto = UserInfoRequestDto.builder()
                .name("유동현2")
                .build();

        user.updateUserInfo(requestDto);

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> newsfeed.userValidate(user));
        assertEquals(ErrorType.NO_AUTHENTICATION, exception.getErrorType());
    }
}