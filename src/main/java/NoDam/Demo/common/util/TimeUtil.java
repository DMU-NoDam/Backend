package NoDam.Demo.common.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

    // 분 단위가 있으면 다음 정각으로 올림 (ex: 1:10 -> 2:00, 2:00 -> 2:00)
    public static LocalTime ceilToNextHour(LocalTime time) {
        if (time == null) return null;
        if (time.getMinute() == 0 && time.getSecond() == 0) return time;
        return time.plusHours(1).truncatedTo(ChronoUnit.HOURS);
    }

    // "yyyy-MM-dd HH:mm" 형식에서 시간(LocalTime) 파싱
    public static LocalTime parseTimeFromDateTime(String dateTimeStr) {
        if (dateTimeStr == null || !dateTimeStr.contains(" ")) return null;
        return LocalTime.parse(dateTimeStr.split(" ")[1], FORMATTER);
    }
}
