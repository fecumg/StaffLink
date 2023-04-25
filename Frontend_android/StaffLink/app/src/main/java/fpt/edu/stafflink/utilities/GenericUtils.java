package fpt.edu.stafflink.utilities;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        if (object == null) {
            return -1;
        }
        for (Object listObject: objects) {
            if (object.equals(listObject)) {
                return i;
            }
            i ++;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getParentObject(T object, String parentField) {
        if (StringUtils.isNotEmpty(parentField)) {
            Class<T> clazz = (Class<T>) object.getClass();
            try {
                Field field = clazz.getDeclaredField(parentField);
                field.setAccessible(true);
                Object value =  field.get(object);
                if (value != null && clazz.isInstance(value)) {
                    return clazz.cast(value);
                } else {
                    return null;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static <T> List<T> getChildObjects(T object, List<T> objects, String parentField) {
        if (StringUtils.isNotEmpty(parentField)) {
            return objects.stream()
                    .filter(child -> getParentObject(child, parentField) != null && getParentObject(child, parentField).equals(object))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public static <T> int getObjectLevel(T object, String parentField, int initialLevel) {
        T parent = getParentObject(object, parentField);
        if (parent != null) {
            return getObjectLevel(parent, parentField, ++ initialLevel);
        } else {
            return initialLevel;
        }
    }

    public static <T> List<T> rearrange(List<T> objects, T parent, String parentField) {
        List<T> rearrangedList = new ArrayList<>();
        objects.forEach(object -> {
            T browsedObjectParent = getParentObject(object, parentField);
            if ((browsedObjectParent == null && parent == null) || (browsedObjectParent != null && browsedObjectParent.equals(parent))) {
                rearrangedList.add(object);
                rearrangedList.addAll(rearrange(objects, object, parentField));
            }
        });
        return rearrangedList;
    }
}
