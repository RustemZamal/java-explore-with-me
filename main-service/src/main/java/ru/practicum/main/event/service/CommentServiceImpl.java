package ru.practicum.main.event.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.dto.CommentDtoIn;
import ru.practicum.main.event.dto.CommentFullDto;
import ru.practicum.main.event.enums.CommentState;
import ru.practicum.main.event.enums.EventState;
import ru.practicum.main.event.mapper.CommentMapper;
import ru.practicum.main.event.model.Comment;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.QComment;
import ru.practicum.main.event.repository.CommentRepository;
import ru.practicum.main.exeption.BadRequest;
import ru.practicum.main.exeption.ConflictException;
import ru.practicum.main.exeption.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.service.UserService;
import ru.practicum.main.util.QPredicates;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final UserService userService;

    private final EventService eventService;

    @Override
    @Transactional
    public CommentDto postCommentViaPrivate(CommentDtoIn commentDtoIn, Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new BadRequest("Comment can be left only on a published event");
        }

        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDtoIn, user, event)));
    }

    @Override
    @Transactional
    public CommentDto patchCommentViaPrivate(CommentDtoIn commentDtoIn, Long userId, Long eventId, Long commentId) {
        userService.getUserById(userId);
        eventService.getEventById(eventId);
        Comment comment = getCommentById(commentId);

        checkUserIsOwnerComment(userId, comment, "edit");
        belongCommentToEvent(comment.getEvent().getId(), eventId);

        comment.setText(commentDtoIn.getText());
        comment.setState(CommentState.EDITED);

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentViaPrivate(Long userId, Long eventId, Long commentId) {
        userService.getUserById(userId);
        eventService.getEventById(eventId);
        getCommentById(commentId);

        checkUserIsOwnerComment(userId, getCommentById(commentId), "delete");
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto getCommentViaPrivate(Long authorId, Long commentId) {
        userService.getUserById(authorId);
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, authorId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "User with id=%d doesn't have such comment with id=%d.", authorId, commentId)));
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getAllOwnerCommentsViaPrivate(
            Long authorId,
            Long eventId,
            String text,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Pageable page) {

        checkRangeTime(rangeStart, rangeEnd);
        userService.getUserById(authorId);
        Predicate pred = makeCommentPredicate(authorId, eventId, text, rangeStart, rangeEnd);

        return commentRepository.findAll(pred, page)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getAllCommentsViaPublic(
            Long eventId,
            String text,
            String authorName,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Pageable page) {

        checkRangeTime(rangeStart, rangeEnd);
        Predicate pred = makePublicCommentPredicate(eventId, text, authorName, rangeStart, rangeEnd);

        if (pred != null) {
            return commentRepository.findAll(pred, page)
                    .stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList());
        }

        return commentRepository.findAll(page)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getCommentByIdViaPublic(Long commentId) {
        return CommentMapper.toCommentDto(getCommentById(commentId));
    }

    @Override
    @Transactional
    public CommentFullDto patchCommentViaAdmin(CommentDtoIn commentDtoIn, Long commentId, Long eventId) {
        Comment comment = getCommentById(commentId);
        eventService.getEventById(eventId);

        belongCommentToEvent(comment.getEvent().getId(), eventId);

        comment.setText(commentDtoIn.getText());
        comment.setState(CommentState.EDITED_BY_ADMIN);

        return CommentMapper.toCommentFullDto(comment);
    }

    @Override
    public CommentFullDto getCommentViaAdmin(Long commentId) {
        return CommentMapper.toCommentFullDto(getCommentById(commentId));
    }

    @Override
    public List<CommentFullDto> getAllCommentsViaAdmin(
            Long eventId,
            Long authorId,
            String text,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Pageable page) {

        checkRangeTime(rangeStart, rangeEnd);
        Predicate pred = makeCommentPredicate(authorId, eventId, text, rangeStart, rangeEnd);

        if (pred != null) {
            return commentRepository.findAll(pred, page)
                    .stream()
                    .map(CommentMapper::toCommentFullDto)
                    .collect(Collectors.toList());
        }
        return commentRepository.findAll(page)
                .stream()
                .map(CommentMapper::toCommentFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCommentViaAdmin(Long commentId) {
        getCommentById(commentId);
        commentRepository.deleteById(commentId);
    }

    private Predicate makePublicCommentPredicate(
            Long eventId,
            String text,
            String authorName,
            LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        QComment comment = QComment.comment;

        return QPredicates.build()
                .add(eventId != null ? comment.id.eq(eventId) : null)
                .add(!text.isEmpty() ? comment.text.containsIgnoreCase(text) : null)
                .add(!authorName.isEmpty() ? comment.author.name.containsIgnoreCase(authorName) : null)
                .add(rangeStart != null ? comment.created.goe(rangeStart) : null)
                .add(rangeEnd != null ? comment.created.loe(rangeEnd) : null)
                .buildAnd();
    }

    private Predicate makeCommentPredicate(
            Long authorId,
            Long eventId,
            String text,
            LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        QComment comment = QComment.comment;

        return QPredicates.build()
                .add(comment.author.id.eq(authorId))
                .add(eventId != null ? comment.event.id.eq(eventId) : null)
                .add(!text.isEmpty() ? comment.text.containsIgnoreCase(text) : null)
                .add(rangeStart != null ? comment.created.goe(rangeStart) : null)
                .add(rangeEnd != null ? comment.created.loe(rangeEnd) : null)
                .buildAnd();
    }

    private void checkUserIsOwnerComment(Long userId, Comment comment, String text) {
        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException(
                    String.format("The user with id=%d cannot %s comment as he/she didn't leave it.", userId, text));
        }
    }

    private void belongCommentToEvent(Long comEventId, Long eventId) {
        if (!comEventId.equals(eventId)) {
            throw new ConflictException(String.format(
                    "The comment with id=%d doesn't belong to event wit id=%d", comEventId, eventId));
        }
    }

    private void checkRangeTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ConflictException(String.format(
                    "Invalid range date, the start=%s must be earlier than the end=%s", rangeStart, rangeEnd));
        }
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("The comment with id=%d was not found", commentId)));
    }
}
