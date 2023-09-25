package world.trecord.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class TimeUtils {
    public static LocalDate toLocalDate(LocalDateTime localDateTime) {
        return Objects.nonNull(localDateTime) ? localDateTime.toLocalDate() : null;
    }
}
