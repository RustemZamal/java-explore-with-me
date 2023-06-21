package ru.practicum.main.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest extends PageRequest {

    private final int from;

    public OffsetPageRequest(int from, int size, Sort sort) {
        super(from, size, sort);
        this.from = from;
    }

    public OffsetPageRequest(int from, int size) {
        super(from, size, Sort.unsorted());
        this.from = from;
    }

    @Override
    public long getOffset() {
        return from;
    }
}
