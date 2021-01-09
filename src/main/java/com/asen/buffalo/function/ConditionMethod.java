package com.asen.buffalo.function;

/**
 * @author Asen
 * @date 2020/12/23
 */
public interface ConditionMethod<R> {
    /**
     * 需要执行的函数逻辑
     *
     * @return 执行结果
     */
    R invoke();
}