package com.sparta.javafeed.entity;

import com.sparta.javafeed.dto.SignupRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    private Comment comment;

    @BeforeEach
    void setUp() {
        User user = setUser();
        user.setId(1L);
        Newsfeed newsfeed = new Newsfeed("title", "description", user);
        comment = new Comment(user, newsfeed, "description");
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
    @DisplayName("사용자 검증 성공")
    void validateSuccess() {
        // given
        User user = setUser();
        user.setId(1L);

        // when, then
        comment.validate(user);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateSuccess() {
        // given
        String updateDescription = "updateDescription";

        // when
        comment.update(updateDescription);

        // then
        assertEquals(updateDescription, comment.getDescription());
    }

    @Test
    @DisplayName("좋아요 수 증가 성공")
    void increaseLikeCntSuccess() {
        // given
        int increaseCnt = 10;

        // when
        for (int i = 0; i < increaseCnt; i++) {
            comment.increaseLikeCnt();
        }

        // then
        assertEquals(increaseCnt, comment.getLikeCnt());
    }

    @Test
    @DisplayName("좋아요 수 감소 성공")
    void decreaseLikeCntSuccess() {
        // given
        int increaseCnt = 10;
        int decreaseCnt = 5;
        for (int i = 0; i < increaseCnt; i++) {
            comment.increaseLikeCnt();
        }

        // when
        for (int i = 0; i < decreaseCnt; i++) {
            comment.decreaseLikeCnt();
        }

        // then
        assertEquals(increaseCnt-decreaseCnt, comment.getLikeCnt());
    }
}