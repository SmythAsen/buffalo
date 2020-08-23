package com.asen.buffalo.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 可序列化的Function
 *
 * @author Asen
 * @version 1.0.3
 * @since 2020/08/23
 */
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
