package com.yangjie.spring.farmework.annoation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})//用于描述类、接口(包括注解类型) 或enum声明
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YJController {
    String value() default "";
}
