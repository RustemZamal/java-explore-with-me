package ru.practicum.stats.sevice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.mapper.EndpointMapper;
import ru.practicum.common.model.EndpointHitDto;
import ru.practicum.common.model.ViewStats;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public EndpointHitDto addHit(EndpointHitDto endpointHitDto) {
        return EndpointMapper.mapToEndpointHitDto(statsRepository.save(EndpointMapper.mapToEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException(String
                    .format("Invalid time interval, the start=%s cannot be later than end=%s", start, end));
        }

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statsRepository.getAllStatsByDistinctIp(start, end);
            } else {
                return statsRepository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                return statsRepository.getStatsByUrisDistinctIps(uris, start, end);
            } else {
                return statsRepository.getStatsByUris(uris, start, end);
            }
        }
    }

}
