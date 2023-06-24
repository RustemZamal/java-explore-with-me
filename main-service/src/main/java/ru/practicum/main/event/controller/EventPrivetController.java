package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.dto.CommentDtoIn;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventRequestStatus;
import ru.practicum.main.event.dto.EventRequestStatusResult;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.EventUserRequestDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.ParticipationRequestDto;
import ru.practicum.main.event.service.CommentService;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.event.service.RequestService;
import ru.practicum.main.util.OffsetPageRequest;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivetController {

    private final EventService eventService;

    private final RequestService requestService;

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto creatEventViaPrivet(@Valid @RequestBody NewEventDto newEventDto, @PathVariable Long userId) {
        return eventService.createEventByUser(newEventDto, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvent(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam (defaultValue = "10") int size) {
        return eventService.getAllEventsByUser(userId, new OffsetPageRequest(from, size));
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByIdViaPrivet(@PathVariable Long userId,@PathVariable Long eventId) {
        return eventService.getEventByIdViaPrivet(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto patchEventViaPrivet(
            @RequestBody @Valid EventUserRequestDto eventUserRequestDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return eventService.patchEventViaPrivet(eventUserRequestDto, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getEventRequestsByEventOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusResult patchEventRequest(
            @RequestBody EventRequestStatus eventRequestStatus,
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return requestService.patchEventRequestByEventOwner(eventRequestStatus, userId, eventId);
    }

    @PostMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto postCommentViaPrivet(
            @RequestBody @Valid CommentDtoIn newCommentDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return commentService.postCommentViaPrivate(newCommentDto, userId, eventId);
    }

    @PatchMapping("/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto patchCommentViaPrivet(
            @RequestBody @Valid CommentDtoIn newCommentDto,
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long commentId) {
        return commentService.patchCommentViaPrivate(newCommentDto, userId, eventId, commentId);
    }

    @DeleteMapping("/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentViaPrivate(@PathVariable Long userId, @PathVariable Long eventId, @PathVariable Long commentId) {
        commentService.deleteCommentViaPrivate(userId, eventId, commentId);
    }
}
