package ru.practicum.main.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoIn {

    @NotBlank(message = "Comment cannot be empty&")
    @Size(min = 4, max = 200, message = "The text length must be at least 4 characters and no more than 200 characters")
    private String text;
}
