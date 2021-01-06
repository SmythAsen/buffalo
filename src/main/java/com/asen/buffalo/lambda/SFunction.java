package com.asen.buffalo.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 可序列化的Function
 *
 * @author Asen
 * @since 1.0.3
 */
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
