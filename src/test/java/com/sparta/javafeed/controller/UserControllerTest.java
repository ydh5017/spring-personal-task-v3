package com.sparta.javafeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.javafeed.config.SecurityConfig;
import com.sparta.javafeed.dto.PasswordReqeustDto;
import com.sparta.javafeed.dto.PasswordUpdateDto;
import com.sparta.javafeed.dto.SignupRequestDto;
import com.sparta.javafeed.dto.UserInfoRequestDto;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.jwt.JwtUtil;
import com.sparta.javafeed.security.UserDetailsImpl;
import com.sparta.javafeed.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

;

@WebMvcTest(value = UserController.class,
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = SecurityConfig.class
            )
        })
class UserControllerTest {

    private MockMvc mockMvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

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
    @DisplayName("회원 가입 성공")
    void signupUserSuccess() throws Exception {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .accountId("user111111")
                .password("1q2w3e4r!@#$")
                .email("ydh001027@gmail.com")
                .name("유동현")
                .build();

        String signInfo = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signInfo))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deactiveUserSuccess() throws Exception {
        //given
        this.mockUserSetup();
        PasswordReqeustDto reqeustDto = new PasswordReqeustDto("1q2w3e4r!@#$");

        String passwordInfo = objectMapper.writeValueAsString(reqeustDto);
        System.out.println(passwordInfo);

        // when, then
        mockMvc.perform(patch("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordInfo)
                .principal(mockPrincipal))
                .andExpect(status().isAccepted())
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() throws Exception {
        // given
        this.mockUserSetup();

        // when, then
        mockMvc.perform(post("/users/logout")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 상세정보 조회 성공")
    void getUserSuccess() throws Exception {
        // given
        this.mockUserSetup();

        // when, then
        mockMvc.perform(get("/users")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateUserSuccess() throws Exception {
        // given
        this.mockUserSetup();

        UserInfoRequestDto requestDto = UserInfoRequestDto.builder()
                .name("유동현2")
                .intro("한 줄 소개")
                .build();

        String userInfo = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userInfo)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void updatePasswordSuccess() throws Exception {
        // given
        this.mockUserSetup();

        PasswordUpdateDto requestDto = new PasswordUpdateDto("1q2w3e4r!@#$", "1qaz2wsx3edc!@#");
        String passwordInfo = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(patch("/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordInfo)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 프로필 이미지 업로드 성공")
    void uploadProfileSuccess() throws Exception {
        // given
        this.mockUserSetup();

        // when, then
        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/users/profile")
                        .part(new MockPart("file", "profile.jpg", null, MediaType.IMAGE_PNG))
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andDo(print());
    }
}