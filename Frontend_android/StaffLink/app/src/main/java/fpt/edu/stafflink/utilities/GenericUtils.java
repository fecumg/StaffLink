package fpt.edu.stafflink.utilities;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class GenericUtils {

    public static String getFieldValue(Object object, String fieldName) {
        if (StringUtils.isEmpty(fieldName) || object == null) {
            return "";
        }
        Class<?> clazz = object.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value =  field.get(object);
            if (value != null) {
                return value.toString();
            } else {
                return "";
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getObjectId(Object object) {
        try {
            if (object == null) {
                return 0;
            }
            Method method = object.getClass().getMethod("getId");
            Object idObject = method.invoke(object);
            if (idObject instanceof Integer) {
                return (int) idObject;
            }
            return 0;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getStringId(Object object) {
        try {
            if (object == null) {
                return "";
            }
            Method method = object.getClass().getMethod("getId");
            Object idObject = method.invoke(object);
            if (idObject instanceof String) {
                return (String) idObject;
            }
            return "";
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getIndexOf(Object object, List<?> objects) {
        int i = 0;
        for (Object listObject: objects) {
            if (object.equals(listObject)) {
                return i;
            }
            i ++;
        }
        return -1;
    }
}
