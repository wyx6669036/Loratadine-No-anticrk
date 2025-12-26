package shop.xmz.lol.loratadine.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static Field textComponentText;

/*    public static void init() {
        for (Field field : TextComponent.class.getDeclaredFields()) {
            if (field.getType() == String.class) {
                textComponentText = field;
            }
        }
    }*/

    public static Object invoke(Method method, Object ins, Object ... parma) {
        Object value = null;
        try {
            final boolean canAccess = method.canAccess(ins);

            if (!canAccess)
                method.setAccessible(true);

            value =  method.invoke(ins, parma);

            if (!canAccess)
                method.setAccessible(false);
        } catch (Throwable ignored) {}

        return value;
    }

    public static void set(Field field, Object ins, Object obj) {
        try {
            final boolean canAccess = field.canAccess(ins);

            if (!canAccess)
                field.setAccessible(true);

            field.set(ins, obj);

            if (!canAccess)
                field.setAccessible(false);
        } catch (Throwable ignored) {}
    }
}
