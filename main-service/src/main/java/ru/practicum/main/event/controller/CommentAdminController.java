package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.CommentDtoIn;
import ru.practicum.main.event.dto.CommentFullDto;
import ru.practicum.main.event.service.CommentService;
import ru.practicum.main.util.DTFormatter;
import ru.practicum.main.util.OffsetPageRequest;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("admin/comments")
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentService commentService;

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentFullDto patchCommentViaAdmin(
            @RequestBody @Valid CommentDtoIn commentDtoIn,
            @PathVariable Long commentId,
            @RequestParam Long eventId) {
        return commentService.patchCommentViaAdmin(commentDtoIn, commentId, eventId);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentFullDto getCommentViaAdmin(@PathVariable Long commentId) {
        return commentService.getCommentViaAdmin(commentId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentFullDto> getAllCommentsViaAdmin(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "") String text,
            @RequestParam(required = false) @DateTimeFormat(pattern = DTFormatter.DT_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DTFormatter.DT_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return commentService.getAllCommentsViaAdmin(
                eventId,
                authorId,
                text,
                rangeStart, rangeEnd,
                new OffsetPageRequest(from, size, Sort.by("created").descending()));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentViaAdmin(@PathVariable Long commentId) {
        commentService.deleteCommentViaAdmin(commentId);
    }
}
