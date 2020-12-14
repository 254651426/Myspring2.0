package com.yangjie.spring.farmework.beans;

public class YJBeanWrapper {

    private Object wrapperInstance;
    private Class<?> wrappedClass;

    public YJBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrappedClass = wrapperInstance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrappedClass() {
        return wrappedClass;
    }

}
