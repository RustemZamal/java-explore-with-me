package ru.practicum.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.event.enums.EventStateAction;
import ru.practicum.main.util.DTFormatter;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventAdminRequestDto {

    @Size(min = 20, max = 2000, message = "The annotation length must be at least 20 character and no more than 2000 characters")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "The description length must be at least 20 character and no more than 7000 characters")
    private String description;

    @JsonFormat(pattern = DTFormatter.DT_FORMAT, shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "The number of participants must be greater than zero.")
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventStateAction stateAction;

    @Size(min = 3, max = 120, message = "The title length must be at least 3 character and no more than 120 characters")
    private String title;
}
