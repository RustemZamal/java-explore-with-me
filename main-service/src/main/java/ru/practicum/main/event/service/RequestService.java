package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.event.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto createEventRequestByRequester(Long userId, Long eventId);

    List<ParticipationRequestDto> getEventRequestsByRequester(Long userId);

    ParticipationRequestDto cancelEventRequestByRequester(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequestsByEventOwner(Long userId, Long eventId);

    EventRequestStatusUpdateResult patchEventRequestByEventOwner(
            EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
            Long userId,
            Long eventId);
}
