package com.sparta.javafeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.javafeed.config.SecurityConfig;
import com.sparta.javafeed.dto.NewsfeedRequestDto;
import com.sparta.javafeed.dto.SignupRequestDto;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.security.UserDetailsImpl;
import com.sparta.javafeed.service.NewsfeedService;
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

@WebMvcTest(value = NewsfeedController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                )
        })
class NewsfeedControllerTest {

    private MockMvc mockMvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NewsfeedService newsfeedService;

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
    @DisplayName("게시글 생성 성공")
    void createNewsfeedSuccess() throws Exception {
        // given
        mockUserSetup();

        NewsfeedRequestDto requestDto = new NewsfeedRequestDto("title", "description", null);

        String newsfeedInfo = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newsfeedInfo)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getNewsfeedSuccess() throws Exception {
        // given
        String page = "1";

        // when, then
        mockMvc.perform(get("/posts")
                .param("page", page))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateNewsfeedSuccess() throws Exception {
        // given
        mockUserSetup();

        NewsfeedRequestDto requestDto = new NewsfeedRequestDto("title", "description", null);
        String newsfeedInfo = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(put("/posts/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newsfeedInfo)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteNewsfeedSuccess() throws Exception {
        // given
        mockUserSetup();

        // when, then
        mockMvc.perform(delete("/posts/{id}", 1)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }
}