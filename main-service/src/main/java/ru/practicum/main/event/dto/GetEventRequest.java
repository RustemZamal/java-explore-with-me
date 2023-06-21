package ru.practicum.main.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.event.enums.EventSortType;
import ru.practicum.main.event.enums.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GetEventRequest {


    private HttpServletRequest request;
    private String text;
    private List<Long> categories;
    Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private boolean onlyAvailable;
    private EventSortType sort;
    private Integer from;
    private Integer size;

    public static GetEventRequest of(
            HttpServletRequest request,
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            boolean onlyAvailable,
            EventSortType sort,
            Integer from,
            Integer size) {
        GetEventRequest getEventRequest = new GetEventRequest();
        getEventRequest.setRequest(request);
        getEventRequest.setFrom(from);
        getEventRequest.setPaid(paid);
        getEventRequest.setSize(size);
        getEventRequest.setOnlyAvailable(onlyAvailable);
        getEventRequest.setText(text);
        getEventRequest.setRangeEnd(rangeEnd);
        getEventRequest.setRangeStart(rangeStart);
        getEventRequest.setCategories(categories);
        getEventRequest.setSort(sort);

        return getEventRequest;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AdminRequest {

        private List<Long> users;

        private List<EventState> states;
        private List<Long> categories;
        private LocalDateTime rangeStart;
        private LocalDateTime rangeEnd;
        private Integer from;
        private Integer size;
    }

    public static AdminRequest ofAdmin(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size) {
        AdminRequest adminRequest = new AdminRequest();
        adminRequest.setUsers(users);
        adminRequest.setStates(states);
        adminRequest.setCategories(categories);
        adminRequest.setRangeStart(rangeStart);
        adminRequest.setRangeEnd(rangeEnd);
        adminRequest.setFrom(from);
        adminRequest.setSize(size);

        return adminRequest;
    }
}
