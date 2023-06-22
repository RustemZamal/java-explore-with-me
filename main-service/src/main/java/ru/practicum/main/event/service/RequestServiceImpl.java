package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.event.dto.ParticipationRequestDto;
import ru.practicum.main.event.enums.EventState;
import ru.practicum.main.event.enums.RequestStatus;
import ru.practicum.main.event.mapper.RequestMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.Request;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.RequestRepository;
import ru.practicum.main.exeption.ConflictException;
import ru.practicum.main.exeption.BadRequest;
import ru.practicum.main.exeption.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;

    private final UserService userService;

    private final EventService eventService;

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto createEventRequestByRequester(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);
        Long numberParticipants = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() != 0 && numberParticipants >= event.getParticipantLimit()) {
            throw new ConflictException(String.format("Event request with id=%d limit reached", eventId));
        }

        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException(
                    String.format("The event initiator with id=%d cannot add a request to participate " +
                            "in his/her event with id=%d", userId, eventId));
        }

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException(String.format("Cannot participate in the unpublished event with id=%d", eventId));
        }

        Request request = createRequest(user, event);

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByRequester(Long userId) {
        userService.getUserById(userId);
        return RequestMapper.toParticipationRequestDto(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelEventRequestByRequester(Long userId, Long requestId) {
        userService.getUserById(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id=%d was not found", requestId)));

        if (!userId.equals(request.getRequester().getId())) {
            throw new BadRequest(String.format(
                    "User with id=%d cannot cancel request with id=%d as he/she didn't apply fot the event.", userId, requestId));
        }

        Event event = eventService.getEventById(request.getEvent().getId());
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByEventOwner(Long userId, Long eventId) {
        userService.getUserById(userId);

        List<Request> requests = requestRepository.findAllByEventAndOwner(userId, eventId);
        return RequestMapper.toParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult patchEventRequestByEventOwner(
            EventRequestStatusUpdateRequest eventRequest,
            Long userId, Long eventId) {

        userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequest(String.format(
                    "The event initiator with id=%d is not the event owner with id=%d", userId, eventId));
        }

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration() || eventRequest.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        List<Request> requests = requestRepository.findAllByIdIn(eventRequest.getRequestIds());

        if (!requests
                .stream()
                .allMatch(request -> request.getStatus().equals(RequestStatus.PENDING))) {
            throw new BadRequest("The status can be changed only for pending requests");
        }

        List<Request> confirmedList = new ArrayList<>();
        List<Request> rejectedList;

        if (RequestStatus.REJECTED.equals(eventRequest.getStatus())) {
            requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            return new EventRequestStatusUpdateResult(
                    RequestMapper.toParticipationRequestDto(confirmedList),
                    RequestMapper.toParticipationRequestDto(requestRepository.saveAll(requests)));
        }

        int participantLimit = event.getParticipantLimit() - event.getConfirmedRequests();
        for (Request request : requests) {
            if (participantLimit != 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedList.add(request);
                participantLimit--;
            }
        }

        requests.removeAll(confirmedList);
        requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));

        confirmedList = requestRepository.saveAll(confirmedList);
        rejectedList = requestRepository.saveAll(requests);
        event.setConfirmedRequests(event.getConfirmedRequests() + confirmedList.size());
        eventRepository.save(event);

        return new EventRequestStatusUpdateResult(
                RequestMapper.toParticipationRequestDto(confirmedList),
                RequestMapper.toParticipationRequestDto(rejectedList));
    }

    private Request createRequest(User user, Event event) {
        return Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .build();
    }
}
