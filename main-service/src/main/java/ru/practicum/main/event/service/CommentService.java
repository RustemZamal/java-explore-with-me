package ru.practicum.main.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.dto.CommentDtoIn;
import ru.practicum.main.event.dto.CommentFullDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {

    CommentDto postCommentViaPrivate(CommentDtoIn newCommentDto, Long userId, Long eventId);

    CommentDto patchCommentViaPrivate(CommentDtoIn newCommentDto, Long userId, Long eventId, Long commentId);

    void deleteCommentViaPrivate(Long userId, Long eventId, Long commentId);

    CommentDto getCommentViaPrivate(Long authorId, Long commentId);

    List<CommentDto> getAllOwnerCommentsViaPrivate(
            Long authorId,
            Long eventId,
            String text,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable page);

    List<CommentDto> getAllCommentsViaPublic(
            Long eventId,
            String text,
            String authorName,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Pageable page);

    CommentDto getCommentByIdViaPublic(Long commentId);

    CommentFullDto patchCommentViaAdmin(CommentDtoIn commentDtoIn, Long commentId, Long eventId);

    CommentFullDto getCommentViaAdmin(Long commentId);

    List<CommentFullDto> getAllCommentsViaAdmin(
            Long eventId,
            Long authorId,
            String text,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Pageable page);

    void deleteCommentViaAdmin(Long commentId);
}
