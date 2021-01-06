package com.asen.buffalo.validator;

/**
 * 参数校验接口
 *
 * @author Asen
 * @since 1.0.0
 */
public interface Verifiable {
    /**
     * 是否有效
     *
     * @param validateValue 需要被校验的值
     * @return true or false
     */
    boolean isValid(Object validateValue);
}
