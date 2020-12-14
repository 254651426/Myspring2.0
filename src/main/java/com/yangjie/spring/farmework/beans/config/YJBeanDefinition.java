package com.yangjie.spring.farmework.beans.config;

public class YJBeanDefinition {

    private String factorBeanName;
    private String beanClassName;

    public String getFactorBeanName() {
        return factorBeanName;
    }

    public void setFactorBeanName(String factorBeanName) {
        this.factorBeanName = factorBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
