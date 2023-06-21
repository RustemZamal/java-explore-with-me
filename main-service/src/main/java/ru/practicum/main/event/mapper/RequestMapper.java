package ru.practicum.main.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main.event.dto.ParticipationRequestDto;
import ru.practicum.main.event.model.Request;
import ru.practicum.main.util.DTFormatter;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class RequestMapper {

    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .requester(request.getRequester().getId())
                .created(request.getCreated().format(DTFormatter.DATE_TIME_FORMATTER))
                .id(request.getId())
                .event(request.getEvent().getId())
                .status(request.getStatus().name())
                .build();
    }

    public List<ParticipationRequestDto> toParticipationRequestDto(Iterable<Request> requests) {
        List<ParticipationRequestDto> dtos = new ArrayList<>();

        for (Request request : requests) {
            dtos.add(toParticipationRequestDto(request));
        }

        return dtos;
    }
}
