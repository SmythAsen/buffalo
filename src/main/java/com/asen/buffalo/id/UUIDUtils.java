package com.asen.buffalo.id;

import java.util.UUID;

/**
 * UUID工具类
 *
 * @author Asen
 * @since 1.0.0
 */
public class UUIDUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
