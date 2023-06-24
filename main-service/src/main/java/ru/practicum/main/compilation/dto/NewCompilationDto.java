package ru.practicum.main.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private Set<Long> events = new HashSet<>();

    private boolean pinned;

    @NotBlank(message = "Title cannot be empty or contain only space.")
    @Size(min = 1, max = 50, message = "The title length must be at least 1 character and no more than 50 characters")
    private String title;
}
