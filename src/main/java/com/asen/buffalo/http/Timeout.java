package com.asen.buffalo.http;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 超时类
 *
 * @author Asen
 * @since 1.1.1
 */
@Data
@Accessors(chain = true)
public class Timeout {
    private Integer timeout;
    private TimeUnit timeUnit;

    public Timeout(Integer timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }
}
