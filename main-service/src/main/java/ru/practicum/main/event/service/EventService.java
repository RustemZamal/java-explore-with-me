package ru.practicum.main.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.EventRequest;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.EventAdminRequestDto;
import ru.practicum.main.event.dto.EventUserRequestDto;
import ru.practicum.main.event.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EventService {

    EventFullDto createEventByUser(NewEventDto newEventDto, Long userId);

    Event getEventById(Long eventId);

    Collection<EventShortDto> getAllEventsViaPublic(EventRequest req);

    EventFullDto getEventByIdViaPublic(Long id, HttpServletRequest requestURI);

    List<EventShortDto> getEventsShortDto(Set<Long> events);

    List<Event> getEventsByIds(Set<Long> events);

    List<EventFullDto> getEventsViaAdmin(EventRequest.AdminRequest adminReq);

    EventFullDto patchEventViaPrivet(
            EventUserRequestDto eventUserRequestDto,
            Long userId,
            Long eventId);

    List<EventShortDto> getAllEventsByUser(Long userId, Pageable page);

    EventFullDto getEventByIdViaPrivet(Long userId, Long eventId);

    EventFullDto pathEventViaAdmin(EventAdminRequestDto eventAdminRequestDto, Long eventId);
}


