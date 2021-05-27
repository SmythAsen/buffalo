package com.asen.buffalo.function;

/**
 * 条件方法，带输入的方法
 *
 * @author Asen
 * @since 1.1.3
 */
public interface ConditionInputMethod<I, R> extends ConditionMethod<R> {
    /**
     * 处理逻辑
     *
     * @param input 输入
     * @return R
     */
    R invoke(I input);
}