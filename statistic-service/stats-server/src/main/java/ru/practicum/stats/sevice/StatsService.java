package ru.practicum.stats.sevice;

import ru.practicum.common.model.EndpointHitDto;
import ru.practicum.common.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHitDto addHit(EndpointHitDto endpointHitDto);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
