package com.yangjie.spring.farmework.context;

import com.yangjie.spring.farmework.annoation.YJAutowired;
import com.yangjie.spring.farmework.annoation.YJController;
import com.yangjie.spring.farmework.annoation.YJService;
import com.yangjie.spring.farmework.beans.YJBeanWrapper;
import com.yangjie.spring.farmework.beans.config.YJBeanDefinition;
import com.yangjie.spring.farmework.beans.support.YJBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class YJApplicationContext {

    Map<String, YJBeanDefinition> beanDefinitionMap = new HashMap<String, YJBeanDefinition>();

    private Map<String,YJBeanWrapper> factoryBeanInstanceCache = new HashMap<String, YJBeanWrapper>();
    //这个容器好像没有这么用到
    Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();

    private YJBeanDefinitionReader beanDefinitionReader;


    public YJApplicationContext(String contextConfigLocation) {
        //1. 加载配置文件
        beanDefinitionReader = new YJBeanDefinitionReader(contextConfigLocation);

        //2 解析配置文件 把类包装成BeanDefinition
        List<YJBeanDefinition> beanDefinitions = beanDefinitionReader.loadBeanDefinitions();

        //3 BeanDefinition 缓存起来
        doRegistBeanDefinition(beanDefinitions);

        //4 DI
        doAutowrited();
    }

    private void doAutowrited() {
        for (Map.Entry<String, YJBeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }

    }

    private void doRegistBeanDefinition(List<YJBeanDefinition> beanDefinitions) {
        for (YJBeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactorBeanName())) {
                try {
                    throw new Exception(beanDefinition.getFactorBeanName() + "已经存在");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //这个位置不是很明白。getFactorBeanName  getBeanClassName 这两个分别作为KEY 意义是什么？
            beanDefinitionMap.put(beanDefinition.getFactorBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }

    }

    public Object getBean(String beanName) {
        //1.得到value bean的配置文件
        YJBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //2.反射实例化
        Object instance = instantiateBean(beanName, beanDefinition);
        //3. 包装成BeanWrapper
        YJBeanWrapper beanWrapper = new YJBeanWrapper(instance);
        //4、保存到IoC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);
        //di  依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, YJBeanDefinition beanDefinition, YJBeanWrapper beanWrapper) {
        //可能涉及到循环依赖？
        //A{ B b}
        //B{ A b}
        //用两个缓存，循环两次
        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值

        Object instance = beanWrapper.getWrapperInstance();

        Class<?> clazz = beanWrapper.getWrappedClass();

        //在Spring中@Component
        if(!(clazz.isAnnotationPresent(YJController.class) || clazz.isAnnotationPresent(YJService.class))){
            return;
        }

        //把所有的包括private/protected/default/public 修饰字段都取出来
        for (Field field : clazz.getDeclaredFields()) {
            if(!field.isAnnotationPresent(YJAutowired.class)){ continue; }

            YJAutowired autowired = field.getAnnotation(YJAutowired.class);

            //如果用户没有自定义的beanName，就默认根据类型注入
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)){
                //field.getType().getName() 获取字段的类型
                autowiredBeanName = field.getType().getName();
            }

            //暴力访问
            field.setAccessible(true);

            try {
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
                    continue;
                }
                //ioc.get(beanName) 相当于通过接口的全名拿到接口的实现的实例
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }

    }

    //创建真正的实例
    private Object instantiateBean(String beanname, YJBeanDefinition beanDefinition) {
        String calssName = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            if (factoryBeanObjectCache.containsKey(beanname)) {
                instance = factoryBeanObjectCache.get(beanname);
            }
            Class<?> clazz = Class.forName(calssName);
            instance = clazz.newInstance();
            factoryBeanObjectCache.put(beanname, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.beanDefinitionReader.getConfig();
    }
}
