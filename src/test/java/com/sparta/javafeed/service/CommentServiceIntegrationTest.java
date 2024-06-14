package com.sparta.javafeed.service;

import com.sparta.javafeed.dto.CommentRequestDto;
import com.sparta.javafeed.dto.CommentResponseDto;
import com.sparta.javafeed.entity.Comment;
import com.sparta.javafeed.entity.Newsfeed;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.repository.CommentRepository;
import com.sparta.javafeed.repository.NewsfeedRepository;
import com.sparta.javafeed.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NewsfeedService newsfeedService;

    @Autowired
    private NewsfeedRepository newsfeedRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Newsfeed newsfeed;
    private Long commentId;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = userService.findByAccountId("test111111");
        user.updateUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Test
    @Order(1)
    @DisplayName("댓글 등록")
    void addComment() {
        // given
        Newsfeed addNewsfeed = new Newsfeed("title", "description", user);
        newsfeed = newsfeedRepository.save(addNewsfeed);
        String description = "description";

        // when
        CommentResponseDto response = commentService.addComment(user, newsfeed.getId(), description);

        // then
        assertNotNull(response);
        assertEquals(description, response.getDescription());
        commentId = response.getId();
    }

    @Test
    @Order(2)
    @DisplayName("commantId로 댓글 찾기")
    void getComment() {
        // when
        comment = commentService.getComment(commentId);

        // then
        assertNotNull(comment);
    }

    @Test
    @Order(3)
    @DisplayName("댓글 목록 조회")
    void getComments() {
        // when
        List<CommentResponseDto> comments = commentService.getComments(newsfeed.getId());

        System.out.println("@@@@@@@@@@@@@@@@size : " + comments.size());

        // then
        assertNotNull(comments);
        assertEquals(comments.size(), 1);
    }

    @Test
    @Order(4)
    @DisplayName("댓글 수정")
    void updateComment() {
        // given
        CommentRequestDto request = new CommentRequestDto("update description");

        // when
        CommentResponseDto response = commentService.updateComment(commentId, request, user);

        // then
        assertNotNull(response);
        assertEquals(response.getDescription(), request.getDescription());
    }

    @Test
    @Order(5)
    @DisplayName("댓글 삭제")
    void deleteComment() {
        // when
        commentService.deleteComment(commentId, user);
        newsfeedRepository.deleteById(newsfeed.getId());

        // then
        assertNull(commentRepository.findById(commentId).orElse(null));
    }
}