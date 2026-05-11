package NoDam.Demo.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * yyyy-MM-dd 형식의 문자열을 LocalDate로 변환
     */
    public static LocalDate toLocalDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateString, FORMATTER);
    }

    /**
     * LocalDate를 yyyy-MM-dd 형식의 문자열로 변환
     */
    public static String fromLocalDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(FORMATTER);
    }

    /**
     * 시작일 ~ 종료일 사이의 모든 날짜를 List로 반환 (양 끝 포함)
     */
    public static List<LocalDate> toDateRange(String startDate, String endDate) {
        return toLocalDate(startDate)
                .datesUntil(toLocalDate(endDate).plusDays(1))
                .toList();
    }

    public static List<LocalDate> toDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1)).toList();
    }
}
