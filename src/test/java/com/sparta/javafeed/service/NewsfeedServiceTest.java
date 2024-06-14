package com.sparta.javafeed.service;

import com.sparta.javafeed.dto.NewsfeedRequestDto;
import com.sparta.javafeed.dto.NewsfeedResponseDto;
import com.sparta.javafeed.dto.SignupRequestDto;
import com.sparta.javafeed.entity.Newsfeed;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.repository.NewsfeedRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NewsfeedServiceTest {

    @InjectMocks
    private NewsfeedService newsfeedService;

    @Mock
    private NewsfeedRepository newsfeedRepository;

    @Mock
    private FileService fileService;

    private List<Newsfeed> getNewsfeedList() {
        List<Newsfeed> newsfeedList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            newsfeedList.add(new Newsfeed("title", "description", getUser()));
        }
        return newsfeedList;
    }

    private User getUser() {
        SignupRequestDto requestDto =new SignupRequestDto(
                "user111111", "1q2w3e4r!@#$", "유동현", "ydh001027@gmail.com");

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(requestDto.getPassword());

        return new User(requestDto, encodedPassword);
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void saveSuccess() {
        // given
        User user = getUser();
        NewsfeedRequestDto newsfeedRequest = new NewsfeedRequestDto("title", "description", null);
        Newsfeed newsfeed = new Newsfeed("title", "description", user);

        // when
        NewsfeedResponseDto newsfeedResponse = newsfeedService.save(newsfeedRequest, user);

        // then
        assertNotNull(newsfeedResponse);
        assertEquals(user.getAccountId(), newsfeedResponse.getAccountId());
        assertEquals(newsfeedRequest.getTitle(), newsfeedResponse.getTitle());
        assertEquals(newsfeedRequest.getDescription(), newsfeedResponse.getDescription());
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void getNewsfeedSuccess() {
        // given
        int page = 1;
        String searchStartDate = "00010101";
        String searchEndDate = "99991231";

        LocalDateTime startDateTime = LocalDate.parse(searchStartDate, DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(0, 0, 0);
        LocalDateTime endDateTime = LocalDate.parse(searchEndDate, DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(23, 59, 59);

        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, 10, sort);

        List<Newsfeed> newsfeedList = getNewsfeedList();

        given(newsfeedRepository
                .findAllByCreatedAtBetweenAndUser_UserStatus(startDateTime, endDateTime, pageable, UserStatus.ACTIVE)
        ).willReturn(newsfeedList);

        // when
        Page<NewsfeedResponseDto> response = newsfeedService.getNewsfeed(page, searchStartDate, searchEndDate);

        // then
        assertNotNull(response);
        assertEquals(newsfeedList.size(), response.getTotalElements());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateNewsfeedSuccess() {
        // given
        User user = getUser();

        Long id = 1L;
        NewsfeedRequestDto newsfeedRequest = new NewsfeedRequestDto("title", "description", null);
        Newsfeed newsfeed = new Newsfeed(id, user, "title", "description", null, null);

        given(newsfeedRepository.findById(id))
                .willReturn(Optional.of(newsfeed));

        // when
        Long newsfeedId = newsfeedService.updateNewsfeed(id, newsfeedRequest, user);

        // then
        assertNotNull(newsfeedId);
        assertEquals(id, newsfeedId);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteNewsfeedSuccess() {
        // given
        User user = getUser();

        Long id = 1L;
        Newsfeed newsfeed = new Newsfeed(id, user, "title", "description", null, null);

        given(newsfeedRepository.findById(id))
                .willReturn(Optional.of(newsfeed));

        // when
        Long newsfeedId = newsfeedService.deleteNewsfeed(id, user);

        // then
        assertNotNull(newsfeedId);
        assertEquals(id, newsfeedId);
    }

    @Test
    @DisplayName("게시글 찾기 성공")
    void findNewsfeedSuccess() {
        // given
        Long id = 1L;
        Newsfeed newsfeed = new Newsfeed(id, getUser(), "title", "description", null, null);

        given(newsfeedRepository.findByIdAndUser_UserStatus(id, UserStatus.ACTIVE))
                .willReturn(Optional.of(newsfeed));

        // when
        Newsfeed getNewsfeed = newsfeedService.getNewsfeed(id);

        //then
        assertNotNull(getNewsfeed);
        assertEquals(newsfeed, getNewsfeed);
    }
}