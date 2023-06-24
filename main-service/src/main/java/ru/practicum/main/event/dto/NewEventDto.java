package ru.practicum.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000, message = "The annotation length must be at least 50 character and no more than 2000 characters")
    private String annotation;

    private Long category;
    @Size(min = 20, max = 7000, message = "The description length must be at least 20 character and no more than 7000 characters")
    @NotBlank
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventDate;

    private LocationDto location;

    private boolean paid;

    @PositiveOrZero(message = "The number of participants must be greater than zero.")
    private int participantLimit = 0;

    private boolean requestModeration = true;

    @Size(min = 3, max = 120, message = "The title length must be at least 3 character and no more than 120 characters")
    private String title;
}
