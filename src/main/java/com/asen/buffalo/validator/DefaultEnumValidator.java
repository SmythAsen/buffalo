package com.asen.buffalo.validator;

import com.google.common.collect.Lists;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.List;

/**
 * 枚举类校验器
 *
 * @author Asen
 * @version 1.0.0
 * @since 2020/05/19
 */
public class DefaultEnumValidator implements ConstraintValidator<EnumValidator, Object> {

    private List<Enum<? extends Verifiable>> valueList = null;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        valueList = Lists.newArrayList();
        Class<? extends Enum<? extends Verifiable>> enumClass = constraintAnnotation.value();
        Enum<? extends Verifiable>[] enumValArr = enumClass.getEnumConstants();
        Collections.addAll(valueList, enumValArr);
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        for (Enum<? extends Verifiable> enumVal : valueList) {
            Verifiable verifiable = (Verifiable) enumVal;
            if (verifiable.isValid(o)) {
                return true;
            }
        }
        return false;
    }
}
