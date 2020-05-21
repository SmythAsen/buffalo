package com.asen.buffalo.validator;

/**
 * @description:
 * @author: Asen
 * @create: 2019/09/19
 */
public interface Verifiable {
    /**
     * 是否有效
     *
     * @param validateValue 需要被校验的值
     * @return
     */
    boolean isValid(Object validateValue);
}
