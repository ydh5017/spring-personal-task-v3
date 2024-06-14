package com.sparta.javafeed.service;

import com.sparta.javafeed.dto.NewsfeedRequestDto;
import com.sparta.javafeed.dto.NewsfeedResponseDto;
import com.sparta.javafeed.entity.Newsfeed;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.repository.NewsfeedRepository;
import com.sparta.javafeed.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NewsfeedServiceIntegrationTest {

    @Autowired
    private NewsfeedService newsfeedService;

    @Autowired
    private NewsfeedRepository newsfeedRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Newsfeed newsfeed;
    private Long newsfeedId;

    @BeforeEach
    void setUp() {
        user = userService.findByAccountId("test111111");
        user.updateUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Test
    @Order(1)
    @DisplayName("게시글 등록")
    void save() {
        // given
        NewsfeedRequestDto request = new NewsfeedRequestDto("title", "description", null);

        // when
        NewsfeedResponseDto response = newsfeedService.save(request, user);
        newsfeedId = response.getId();

        // then
        assertNotNull(response);
        assertEquals(response.getAccountId(), user.getAccountId());
        assertEquals(response.getTitle(), request.getTitle());
        assertEquals(response.getDescription(), request.getDescription());
    }

    @Test
    @Order(2)
    @DisplayName("id로 게시글 찾기")
    void getNewsfeed() {
        // given
        // when
        newsfeed = newsfeedService.getNewsfeed(newsfeedId);

        // then
        assertNotNull(newsfeed);
        assertEquals(newsfeed.getUser().getAccountId(), user.getAccountId());
    }

    @Test
    @Order(3)
    @DisplayName("게시글 목록 조회")
    void getNewsfeedList() {
        // given
        int page = 1;
        String searchStartDate = null;
        String searchEndDate = null;

        // when
        Page<NewsfeedResponseDto> newsfeedList = newsfeedService.getNewsfeed(page, searchStartDate, searchEndDate);

        // then
        assertNotNull(newsfeedList);
    }

    @Test
    @Order(4)
    @DisplayName("게시글 수정")
    void updateNewsfeed() {
        // given
        NewsfeedRequestDto request =
                new NewsfeedRequestDto("updateTitle", "updateDescription", null);

        // when
        Long updateId = newsfeedService.updateNewsfeed(newsfeedId, request, user);

        // then
        newsfeed = newsfeedRepository.findById(updateId).orElse(null);
        assertNotNull(updateId);
        assertEquals(newsfeedId, updateId);
        assertEquals(newsfeed.getTitle(), request.getTitle());
        assertEquals(newsfeed.getDescription(), request.getDescription());
    }

    @Test
    @Order(5)
    @DisplayName("게시글 삭제")
    void deleteNewsfeed() {
        // when
        Long deleteId = newsfeedService.deleteNewsfeed(newsfeedId, user);

        // then
        assertNotNull(deleteId);
        assertEquals(newsfeedId, deleteId);
        assertNull(newsfeedRepository.findById(deleteId).orElse(null));
    }
}