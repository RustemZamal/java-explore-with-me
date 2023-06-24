package ru.practicum.main.util;

import java.time.format.DateTimeFormatter;

public class DTFormatter {

    public static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);
}
