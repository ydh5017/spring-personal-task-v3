package com.sparta.javafeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.javafeed.config.SecurityConfig;
import com.sparta.javafeed.dto.CommentRequestDto;
import com.sparta.javafeed.dto.SignupRequestDto;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.security.UserDetailsImpl;
import com.sparta.javafeed.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CommentController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                )
        })
class CommentControllerTest {

    private MockMvc mockMvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    private void mockUserSetup() {
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .accountId("user111111")
                .password("1q2w3e4r!@#$")
                .email("ydh001027@gmail.com")
                .name("유동현")
                .build();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(requestDto, encodedPassword);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        mockPrincipal = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void addCommentSuccess() throws Exception {
        // given
        mockUserSetup();

        CommentRequestDto requestDto = new CommentRequestDto("description");
        String commentInfo = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(post("/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentInfo)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getCommentsSuccess() throws Exception {
        // when, then
        mockMvc.perform(get("/posts/1/comments"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateCommentSuccess() throws Exception {
        // given
        mockUserSetup();

        CommentRequestDto requestDto = new CommentRequestDto("updateDescription");
        String commentInfo = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(put("/posts/1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentInfo)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteCommentSuccess() throws Exception {
        // given
        mockUserSetup();

        // when, then
        mockMvc.perform(delete("/posts/1/comments/1")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }
}