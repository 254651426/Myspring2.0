package com.yangjie.spring.farmework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class YJHandlerMapping {

    private Pattern pattern;//URL
    private Method method; //对应的Method
    private Object controller;//Method对应的实例对象

    public YJHandlerMapping(Pattern pattern, Object instance, Method method) {
        this.pattern = pattern;
        this.method = method;
        this.controller = instance;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

}
