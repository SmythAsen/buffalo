package com.asen.buffalo.reflect;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * 获取反射字段的GET、SET方法名
 *
 * @author Asen
 * @version 1.0.0
 * @since 2020/05/19
 **/
public class FieldUtils {
    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";
    private static final String SET_PREFIX = "set";

    public static String getGetMethodName(Field field) {
        return getGetMethodName(field.getName(), field.getType());
    }

    public static String getSetMethodName(Field field) {
        return getSetMethodName(field.getName(), field.getType());
    }

    public static String getSetMethodName(String fieldName, Class<?> fieldType) {
        // 如果字段类型为boolean类型，并且字段是以“is”开头的，则需要去掉is
        if (fieldType.equals(Boolean.class)) {
            if (fieldName.startsWith(IS_PREFIX)) {
                fieldName = fieldName.substring(2);
            }
        }
        return SET_PREFIX + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String getGetMethodName(String fieldName, Class<?> fieldType) {
        // 如果字段类型为boolean类型，并且字段是以“is”开头的，则需要去掉is
        if (fieldType.equals(Boolean.class)) {
            if (fieldName.startsWith(IS_PREFIX)) {
                fieldName = fieldName.substring(2);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fieldName.substring(0, 1).toUpperCase())
                .append(fieldName.substring(1));
        return fieldType.equals(boolean.class) ? stringBuilder.insert(0, IS_PREFIX).toString() : stringBuilder.insert(0, GET_PREFIX).toString();
    }

    public static String methodToProperty(String methodName) {
        if (methodName.startsWith(IS_PREFIX)) {
            methodName = methodName.substring(2);
        } else if (methodName.startsWith(GET_PREFIX) || methodName.startsWith(SET_PREFIX)) {
            methodName = methodName.substring(3);
        }
        if (methodName.length() == 1 || (methodName.length() > 1 && !Character.isUpperCase(methodName.charAt(1)))) {
            methodName = methodName.substring(0, 1).toLowerCase(Locale.ENGLISH) + methodName.substring(1);
        }
        return methodName;
    }

    public static boolean isProperty(String name) {
        return isGetter(name) || isSetter(name);
    }

    public static boolean isGetter(String name) {
        return (name.startsWith(GET_PREFIX) && name.length() > 3) || (name.startsWith(IS_PREFIX) && name.length() > 2);
    }

    public static boolean isSetter(String name) {
        return name.startsWith(SET_PREFIX) && name.length() > 3;
    }
}
