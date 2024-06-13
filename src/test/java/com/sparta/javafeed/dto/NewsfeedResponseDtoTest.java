package com.sparta.javafeed.dto;

import com.sparta.javafeed.entity.Newsfeed;
import com.sparta.javafeed.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class NewsfeedResponseDtoTest {

    private NewsfeedResponseDto newsfeedResponseDto;

    @BeforeEach
    void setUp() {
        Newsfeed newsfeed = setNewsfeed();
        newsfeedResponseDto = new NewsfeedResponseDto(newsfeed);
    }

    private Newsfeed setNewsfeed() {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .accountId("user111111")
                .password("1q2w3e4r!@#$")
                .email("ydh001027@gmail.com")
                .name("유동현")
                .build();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(requestDto, encodedPassword);

        return new Newsfeed("title", "description", user);
    }

    @Test
    @DisplayName("Newsfeed Entity Dto로 변환 성공")
    void toDtoSuccess() {
        // given
        Newsfeed newsfeed = setNewsfeed();

        // when
        NewsfeedResponseDto responseDto = NewsfeedResponseDto.toDto(newsfeed);

        // then
        assertEquals(newsfeedResponseDto.getAccountId(), responseDto.getAccountId());
        assertEquals(newsfeedResponseDto.getTitle(), responseDto.getTitle());
        assertEquals(newsfeedResponseDto.getDescription(), responseDto.getDescription());
    }
}