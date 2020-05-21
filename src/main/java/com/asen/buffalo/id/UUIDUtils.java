package com.asen.buffalo.id;

import java.util.UUID;

/**
 * @description: UUID工具类
 * @author: Asen
 * @create: 2019/03/18
 */
public class UUIDUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
