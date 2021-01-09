package com.asen.buffalo.function;

/**
 * @author Asen
 * @since 1.1.2
 */
public interface ConditionMethod<R> {
    /**
     * 需要执行的函数逻辑
     *
     * @return 执行结果
     */
    R invoke();
}