package com.sparta.javafeed.service;

import com.sparta.javafeed.dto.CommentRequestDto;
import com.sparta.javafeed.dto.CommentResponseDto;
import com.sparta.javafeed.dto.SignupRequestDto;
import com.sparta.javafeed.entity.Comment;
import com.sparta.javafeed.entity.Newsfeed;
import com.sparta.javafeed.entity.User;
import com.sparta.javafeed.enums.UserStatus;
import com.sparta.javafeed.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsfeedService newsfeedService;

    private User getUser() {
        SignupRequestDto requestDto =new SignupRequestDto(
                "user111111", "1q2w3e4r!@#$", "유동현", "ydh001027@gmail.com");

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(requestDto.getPassword());

        User user = new User(requestDto, encodedPassword);
        user.setId(1L);

        return user;
    }

    private Newsfeed getNewsfeed() {
        return new Newsfeed(
                1L, getUser(), "title", "description", null, null);
    }

    private Comment getComment() {
        User user = getUser();
        Newsfeed newsfeed = getNewsfeed();
        String description = "description";

        return new Comment(1L, user, newsfeed, description, 0L);
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void addCommentSuccess() {
        // given
        Comment comment = getComment();

        given(newsfeedService.getNewsfeed(comment.getNewsfeed().getId()))
                .willReturn(comment.getNewsfeed());

        // when
        CommentResponseDto commentResponse = commentService
                .addComment(comment.getUser(), comment.getNewsfeed().getId(), comment.getDescription());

        // then
        assertNotNull(commentResponse);
        assertEquals(commentResponse.getWriter(), comment.getUser().getName());
        assertEquals(commentResponse.getDescription(), comment.getDescription());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getCommentsSuccess() {
        // given
        Comment comment = getComment();
        List<Comment> commentList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            commentList.add(comment);
        }

        given(commentRepository.findAllByNewsfeedIdAndUser_UserStatus(comment.getNewsfeed().getId(), UserStatus.ACTIVE))
                .willReturn(commentList);

        // when
        List<CommentResponseDto> responseList = commentService.getComments(comment.getNewsfeed().getId());

        assertNotNull(responseList);
        assertEquals(responseList.size(), commentList.size());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateCommentSuccess() {
        // given
        Comment comment = getComment();
        CommentRequestDto requestDto = new CommentRequestDto("update description");

        given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(comment));
        given(newsfeedService.getNewsfeed(comment.getNewsfeed().getId()))
                .willReturn(comment.getNewsfeed());

        // when
        CommentResponseDto responseDto = commentService
                .updateComment(comment.getId(), requestDto, comment.getUser());

        // then
        assertNotNull(responseDto);
        assertEquals(responseDto.getWriter(), comment.getUser().getName());
        assertEquals(responseDto.getDescription(), comment.getDescription());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteCommentSuccess() {
        // given
        Comment comment = getComment();

        given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(comment));
        given(newsfeedService.getNewsfeed(comment.getNewsfeed().getId()))
                .willReturn(comment.getNewsfeed());

        // when
        commentService.deleteComment(comment.getId(), comment.getUser());
    }

    @Test
    @DisplayName("댓글 찾기 성공")
    void getCommentSuccess() {
        // given
        Comment comment = getComment();

        given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(comment));

        // when
        Comment getComment = commentService.getComment(comment.getId());

        // then
        assertNotNull(getComment);
        assertEquals(comment, getComment);
    }
}