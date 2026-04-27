package NoDam.Demo.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
}
