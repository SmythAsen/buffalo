package com.asen.buffalo.id;

import java.util.UUID;

/**
 * UUID工具类
 *
 * @author Asen
 * @version 1.0.0
 * @since 2020/05/19
 */
public class UUIDUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
