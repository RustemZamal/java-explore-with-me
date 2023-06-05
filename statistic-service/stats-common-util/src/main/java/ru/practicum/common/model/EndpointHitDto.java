package ru.practicum.common.model;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
public class EndpointHitDto {

    @NotBlank(message = "The app name cannot be empty.")
    private String app;

    @NotBlank(message = "URI cannot be empty.")
    private String uri;

    @NotBlank(message = "IP cannot be empty.")
    private String ip;

    @NotBlank(message = "Timestamp cannot be empty.")
    private String timestamp;
}
