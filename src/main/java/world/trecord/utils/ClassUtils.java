package world.trecord.utils;

import java.util.Objects;

public abstract class ClassUtils {
    public static <T> T getSafeInstance(Object obj, Class<T> clazz) {
        return Objects.nonNull(clazz) && clazz.isInstance(obj) ? clazz.cast(obj) : null;
    }
}
