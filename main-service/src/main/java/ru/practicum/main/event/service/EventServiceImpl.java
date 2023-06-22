package ru.practicum.main.event.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.model.ViewStats;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.service.CategoryService;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.EventRequest;
import ru.practicum.main.event.dto.LocationDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.EventAdminRequestDto;
import ru.practicum.main.event.dto.EventUserRequesDto;
import ru.practicum.main.event.enums.EventSortType;
import ru.practicum.main.event.enums.EventState;
import ru.practicum.main.event.enums.EventStateAction;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.mapper.LocationMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.Location;
import ru.practicum.main.event.model.QEvent;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.LocationRepository;
import ru.practicum.main.exeption.ConflictException;
import ru.practicum.main.exeption.BadRequest;
import ru.practicum.main.exeption.NotFoundException;
import ru.practicum.main.statistic.StatsService;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.service.UserService;
import ru.practicum.main.util.OffsetPageRequest;
import ru.practicum.main.util.QPredicates;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final UserService userService;

    private final CategoryService categoryService;

    private final LocationRepository locationRepository;

    private final StatsService statsService;


    @Override
    @Transactional
    public EventFullDto createEventByUser(NewEventDto newEventDto, Long userId) {
        checkNewEventDate(newEventDto.getEventDate(), LocalDateTime.now().plusHours(2));

        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        Location location = getOrSaveLocation(newEventDto.getLocation());
        Event event = eventRepository.save(EventMapper.toEvent(newEventDto, category, user, location));

        return EventMapper.toEvenFullDto(eventRepository.save(event));
    }


    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    @Override
    public List<EventShortDto> getAllEventsViaPublic(EventRequest req) {
        checkRangeTime(req.getRangeStart(), req.getRangeEnd());

        List<Event> events;
        Map<Long, Long> views;
        Predicate pred = makePublicPredicate(req);

        if (EventSortType.EVENT_DATE.equals(req.getSort())) {
            OffsetPageRequest pageRequest = new OffsetPageRequest(req.getFrom(), req.getSize(), Sort.by("eventDate"));
            events = eventRepository.findAll(pred, pageRequest).getContent();
            views = getViews(events);
            statsService.addHit(req.getRequest());
            return events.stream().map(event ->
                    EventMapper.toEvenShortDto(event, views.getOrDefault(event.getId(), 0L)))
                    .collect(Collectors.toList());
        }

        events = eventRepository.findAll(pred, new OffsetPageRequest(req.getFrom(), req.getSize())).getContent();
        views = getViews(events);
        statsService.addHit(req.getRequest());
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event ->
                        EventMapper.toEvenShortDto(event, views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());

        if (EventSortType.VIEWS.equals(req.getSort()) && !views.isEmpty()) {
            return eventShortDtos.stream().sorted(Comparator.comparing(EventShortDto::getViews).reversed()).collect(Collectors.toList());
        }

        return eventShortDtos;
    }


    @Override
    public EventFullDto getEventByIdViaPublic(Long id, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", id)));

        statsService.addHit(request);
        Map<Long, Long> views = getViews(List.of(event));
        return EventMapper.toEvenFullDto(event, views.getOrDefault(event.getId(), 0L));
    }

    @Override
    public List<Event> getEventsByIds(Set<Long> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        return eventRepository.findAllByIdIn(events);
    }

    @Override
    public List<EventFullDto> getEventsViaAdmin(EventRequest.AdminRequest req) {
        checkRangeTime(req.getRangeStart(), req.getRangeEnd());

        List<Event> events;
        Map<Long, Long> views;
        Predicate predicate = makeAdminPredicate(req);

        if (predicate != null) {
            events = eventRepository.findAll(
                    predicate, new OffsetPageRequest(req.getFrom(), req.getSize())).getContent();
           views = getViews(events);
            return events.stream()
                    .map(event ->
                            EventMapper.toEvenFullDto(event, views.getOrDefault(event.getId(), 0L)))
                    .collect(Collectors.toList());
        }

        events = eventRepository.findAll(new OffsetPageRequest(req.getFrom(), req.getSize())).getContent();
        views = getViews(events);

        return events.stream()
                .map(event ->
                        EventMapper.toEvenFullDto(event, views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto patchEventViaPrivet(EventUserRequesDto eventUserRequesDto, Long userId, Long eventId) {
        checkNewEventDate(eventUserRequesDto.getEventDate(), LocalDateTime.now().plusHours(2));

        Event event = getEventById(eventId);
        userService.getUserById(userId);

        if (EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (eventUserRequesDto.getAnnotation() != null) {
            event.setAnnotation(eventUserRequesDto.getAnnotation());
        }

        if (eventUserRequesDto.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(eventUserRequesDto.getCategory()));
        }

        if (eventUserRequesDto.getDescription() != null) {
            event.setDescription(eventUserRequesDto.getDescription());
        }

        if (eventUserRequesDto.getEventDate() != null) {
            event.setEventDate(eventUserRequesDto.getEventDate());
        }

        if (eventUserRequesDto.getLocation() != null) {
            event.setLocation(getOrSaveLocation(eventUserRequesDto.getLocation()));
        }

        if (eventUserRequesDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventUserRequesDto.getParticipantLimit());
        }

        if (eventUserRequesDto.getPaid() != null) {
            event.setPaid(eventUserRequesDto.getPaid());
        }

        if (eventUserRequesDto.getRequestModeration() != null) {
            event.setRequestModeration(eventUserRequesDto.getRequestModeration());
        }

        if (eventUserRequesDto.getTitle() != null) {
            event.setTitle(eventUserRequesDto.getTitle());
        }

        if (eventUserRequesDto.getStateAction() != null) {
            if (EventStateAction.SEND_TO_REVIEW.equals(eventUserRequesDto.getStateAction())) {
                event.setState(EventState.PENDING);
            } else if (EventStateAction.CANCEL_REVIEW.equals(eventUserRequesDto.getStateAction())) {
                event.setState(EventState.CANCELED);
            }
        }

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getAllEventsByUser(Long userId, Pageable page) {
        userService.getUserById(userId);

        List<Event> events = eventRepository.findByInitiatorId(userId, page);
        Map<Long, Long> views = getViews(events);
        return events.stream().map(event ->
                EventMapper.toEvenShortDto(event, views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByIdViaPrivet(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Event with id=%d was not found", eventId)));

        return EventMapper.toEvenFullDto(event, getViews(List.of(event)).getOrDefault(event.getId(), 0L));
    }

    @Override
    @Transactional
    public EventFullDto pathEventViaAdmin(EventAdminRequestDto eventRequest, Long eventId) {
        checkNewEventDate(eventRequest.getEventDate(), LocalDateTime.now().plusHours(1));

        Event event = getEventById(eventId);

        if (eventRequest.getStateAction() != null && eventRequest.getStateAction().equals(EventStateAction.REJECT_EVENT)
                && event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(
                    String.format("Cannot reject the event because it's not in the right state: %s", event.getState()));
        }

        if (eventRequest.getStateAction() != null && eventRequest.getStateAction().equals(EventStateAction.PUBLISH_EVENT)
                && !event.getState().equals(EventState.PENDING)) {
            throw new ConflictException(
                    String.format("Cannot publish the event because it's not in the right state: %s", event.getState()));
        }

        if (eventRequest.getAnnotation() != null) {
            event.setAnnotation(eventRequest.getAnnotation());
        }

        if (eventRequest.getDescription() != null) {
            event.setDescription(eventRequest.getDescription());
        }

        if (eventRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(eventRequest.getCategory()));
        }

        if (eventRequest.getEventDate() != null) {
            event.setEventDate(eventRequest.getEventDate());
        }

        if (eventRequest.getPaid() != null) {
            event.setPaid(eventRequest.getPaid());
        }

        if (eventRequest.getLocation() != null) {
            event.setLocation(getOrSaveLocation(eventRequest.getLocation()));
        }

        if (eventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(eventRequest.getParticipantLimit());
        }

        if (eventRequest.getRequestModeration() != null) {
            event.setRequestModeration(eventRequest.getRequestModeration());
        }

        if (eventRequest.getTitle() != null) {
            event.setTitle(eventRequest.getTitle());
        }

        if (eventRequest.getStateAction() != null) {
            switch (eventRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
                default: throw new ConflictException(
                        String.format("Field: stateAction. Incorrect action state. Value: %s", eventRequest.getStateAction()));
            }
        }

        return toEventFullDto(eventRepository.save(event));
    }

    private EventFullDto toEventFullDto(Event event) {
        Map<Long, Long> views = getViews(List.of(event));

        return EventMapper.toEvenFullDto(event, views.getOrDefault(event.getId(), 0L));
    }

    private Map<Long, Long> getViews(List<Event> events) {
        Map<Long, Long> statsByEventId = new HashMap<>();

        if (!events.iterator().hasNext()) {
            return statsByEventId;
        }

        List<Event> publishedEvents = events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .collect(Collectors.toList());


        if (publishedEvents.isEmpty()) {
            return statsByEventId;
        }

        LocalDateTime publishedOn = publishedEvents
                .stream()
                .min(Comparator.comparing(Event::getPublishedOn))
                .get().getPublishedOn();

        List<String> uris = publishedEvents.stream()
                .map(Event::getId)
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ViewStats> stats = statsService.getStats(publishedOn, LocalDateTime.now(), uris, true);

        statsByEventId = stats.stream()
                .collect(Collectors.toMap(entity -> (
                        Long.parseLong(entity.getUri().split("/")[2])), ViewStats::getHits));
        return statsByEventId;
    }

    @Override
    public List<EventShortDto> getEventsShortDto(Set<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        Map<Long, Long> views = getViews(events);

        return events.stream()
                .map(event -> EventMapper.toEvenShortDto(event, views.getOrDefault(event.getId(), 0L)
        )).collect(Collectors.toList());
    }


    private Predicate makeAdminPredicate(EventRequest.AdminRequest req) {
        QEvent event = QEvent.event;
        return QPredicates.build()
                .add(req.getUsers() != null ? event.initiator.id.in(req.getUsers()) : null)
                .add(req.getStates() != null ? event.state.in(req.getStates()) : null)
                .add(req.getCategories() != null ? event.category.id.in(req.getCategories()) : null)
                .add(req.getRangeStart() != null ? event.eventDate.goe(req.getRangeStart()) : null)
                .add(req.getRangeEnd() != null ? event.eventDate.loe(req.getRangeEnd()) : null)
                .buildAnd();
    }


    private Predicate makePublicPredicate(EventRequest req) {
        QEvent entity = QEvent.event;
        return QPredicates.build()
                .add(entity.state.eq(EventState.PUBLISHED))
                .add(!req.getText().isBlank() ? entity.annotation.containsIgnoreCase(req.getText()).or(entity.description.containsIgnoreCase(req.getText())) : null)
                .add(req.isOnlyAvailable() ? entity.confirmedRequests.lt(entity.participantLimit).or(entity.participantLimit.eq(0)) : null)
                .add(req.getPaid() != null ? entity.paid.eq(req.getPaid()) : null)
                .add(req.getCategories() == null || req.getCategories().isEmpty() ? null : entity.category.id.in(req.getCategories()))
                .add(req.getRangeStart() != null  && req.getRangeEnd() != null
                        ? entity.eventDate.between(req.getRangeStart(), req.getRangeEnd()) : entity.eventDate.after(LocalDateTime.now()))
                .buildAnd();

    }

    private Location getOrSaveLocation(LocationDto location) {
        return locationRepository.findByLatAndLon(location.getLat(), location.getLon())
                .orElseGet(() -> locationRepository.save(LocationMapper.toLocation(location)));
    }

    private void checkNewEventDate(LocalDateTime newEventDate, LocalDateTime dateBeforeEventStart) {
        if (newEventDate != null && newEventDate.isBefore(dateBeforeEventStart)) {
            throw new BadRequest(String.format(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s", newEventDate));
        }
    }

    private void checkRangeTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && (rangeStart.isAfter(rangeEnd))) {
                throw new BadRequest(
                        String.format("Field: rangeStart. Error: rangeStart cannot be after rangeEnd. Value: %s", rangeStart));
        }
    }
}
