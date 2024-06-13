package com.sparta.javafeed.dto;

import com.sparta.javafeed.entity.Newsfeed;
import com.sparta.javafeed.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class NewsfeedRequestDtoTest {

    private NewsfeedRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new NewsfeedRequestDto("title", "description", null);
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
    @DisplayName("Newsfeed Entity 생성 성공")
    void toEntitySuccess() {
        // given
        User user = setUser();

        Newsfeed newsfeed = requestDto.toEntity(user);

        assertEquals(user, newsfeed.getUser());
        assertEquals(requestDto.getTitle(), newsfeed.getTitle());
        assertEquals(requestDto.getDescription(), newsfeed.getDescription());
    }
}