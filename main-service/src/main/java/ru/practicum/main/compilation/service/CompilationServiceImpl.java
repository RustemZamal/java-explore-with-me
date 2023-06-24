package ru.practicum.main.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.CompilationRequest;
import ru.practicum.main.compilation.mapper.CompilationMapper;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.exeption.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final EventService eventService;

    private final CompilationRepository compilationRepository;

    @Override
    @Transactional
    public CompilationDto createCompilationViaAdmin(NewCompilationDto newCompilationDto) {
        List<Event> eventsByIds = new ArrayList<>();

        if (!newCompilationDto.getEvents().isEmpty()) {
            eventsByIds = eventService.getEventsByIds(newCompilationDto.getEvents());
        }

        Compilation save = compilationRepository.save(CompilationMapper.toCompilation(newCompilationDto, eventsByIds));

        return toCompilationDto(newCompilationDto.getEvents(), save);
    }

    @Override
    @Transactional
    public CompilationDto patchCompilationViaAdmin(CompilationRequest compilationRequest, Long compId) {
        Compilation compilation = getCompilationById(compId);

        if (compilationRequest.getPinned() != null) {
            compilation.setPinned(compilationRequest.getPinned());
        }

        if (compilationRequest.getTitle() != null) {
            compilation.setTitle(compilationRequest.getTitle());
        }

        if (compilationRequest.getEvents() != null) {
            List<Event> events = eventService.getEventsByIds(compilationRequest.getEvents());
            compilation.setEvents(events);
        }

        Compilation savedComp = compilationRepository.save(compilation);

        return toCompilationDto(compilationRequest.getEvents(), savedComp);
    }

    @Override
    @Transactional
    public void deleteCompilationByIdViaAdmin(Long compId) {
        getCompilationById(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAllCompilationsViaPublic(boolean pinned, Pageable page) {
        List<Compilation> compilations;

        if (pinned) {
            compilations = compilationRepository.findAllByPinned(pinned, page);
        } else  {
            compilations = compilationRepository.findAll(page).getContent();
        }

        return toCompilationDto(compilations);
    }

    @Override
    public CompilationDto getCompilationByIdViaPublic(Long compId) {
        Compilation compilation = getCompilationById(compId);
        return toCompilationDto(compilation.getEvents().stream().map(Event::getId).collect(Collectors.toSet()), compilation);
    }

    public Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Compilation with id=%d was not found.", compId)));
    }

    private CompilationDto toCompilationDto(Set<Long> eventIds, Compilation compilation) {
        List<EventShortDto> eventShorts = new ArrayList<>();

        if (eventIds != null) {
            eventShorts = eventService.getEventsShortDto(eventIds);
        }

        return CompilationMapper.toCompilationDto(compilation, eventShorts);
    }

    private List<CompilationDto> toCompilationDto(List<Compilation> compilations) {
        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> eventIds = compilations
                .stream().flatMap(compilation -> compilation.getEvents()
                        .stream()
                        .map(Event::getId))
                .collect(Collectors.toSet());

        List<EventShortDto> eventsShortDto = eventService.getEventsShortDto(eventIds);

        Map<Long, EventShortDto> eventsMap = eventsShortDto.stream()
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        return compilations.stream()
                .map(compilation -> {
                    List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                            .map(event -> eventsMap.get(event.getId()))
                            .collect(Collectors.toList());

                    return CompilationMapper.toCompilationDto(compilation, eventShortDtos);
                })
                .collect(Collectors.toList());
    }

}
