package com.yangjie.spring.farmework.annoation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YJRequestParam {
    String value() default "";
}
