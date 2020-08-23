package com.asen.buffalo.lambda;

import org.junit.Test;

public class LambdaUtilsTest {

    @Test
    public void getPropertyName() {
        String objectN = LambdaUtils.getPropertyName(TestObject::getObjectName);
        String objectV = LambdaUtils.getPropertyName(TestObject::getObjectValue);
        System.out.println(objectN);
        System.out.println(objectV);
    }
}