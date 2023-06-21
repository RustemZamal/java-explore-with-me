package ru.practicum.main.statistic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.client.stats_client.StatisticsClient;
import ru.practicum.common.model.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatisticsClient statisticsClient;

    @Value("${app.name}")
    private String appName;

    private final ObjectMapper mapper;

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        ResponseEntity<Object> stats = statisticsClient.getStats(start, end, uris, unique);
        return mapper.convertValue(stats.getBody(), new TypeReference<>() {});
    }

    public void addHit(HttpServletRequest request) {
        statisticsClient.addHit(appName,request.getRemoteAddr(), request.getRequestURI(), LocalDateTime.now());
    }
}
