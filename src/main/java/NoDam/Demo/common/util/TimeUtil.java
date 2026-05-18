package NoDam.Demo.common.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static LocalTime toLocalTime(String timeString) {
        if (timeString == null || timeString.isBlank()) {
            return null;
        }
        return LocalTime.parse(timeString, FORMATTER);
    }

    public static String fromLocalTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(FORMATTER);
    }

    public static LocalTime addSeconds(LocalTime time, int seconds) {
        if (time == null) {
            return null;
        }
        return time.plusSeconds(seconds);
    }
}
