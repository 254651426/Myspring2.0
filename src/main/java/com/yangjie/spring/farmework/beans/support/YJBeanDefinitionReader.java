package com.yangjie.spring.farmework.beans.support;

import com.yangjie.spring.farmework.beans.config.YJBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class YJBeanDefinitionReader {

    //保存扫描出来的结果
    List<String> regitryBeanClasses = new ArrayList<String>();

    //存蓄配置文件里面扫描出来的类
    private Properties properties = new Properties();

    public YJBeanDefinitionReader(String... contextConfigLocation) {
        doLoadConfig(contextConfigLocation[0]);

        //扫描配置文件中的配置的相关的类
        doScanner(properties.getProperty("package"));
    }

    private void doLoadConfig(String contextConfigLocation) {
        //从项目根目录找到该位置文件并转化成properties文件
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void doScanner(String Package) {
        URL url = this.getClass().getClassLoader().getResource("/" + Package.replaceAll("\\.", "/"));
        File file = new File(url.getFile());

        for (File f : file.listFiles()) {

            //如果是文件夹继续递归
            if (f.isDirectory()) {
                doScanner(Package + "." + f.getName());
            } else {
                if (!f.getName().endsWith(".class")) {
                    continue;
                }
                //.class 替换掉
                String classname = Package + "." + f.getName().replaceAll(".class", "");
                regitryBeanClasses.add(classname);
            }
        }

    }

    public List<YJBeanDefinition> loadBeanDefinitions() {
        List<YJBeanDefinition> result = new ArrayList<YJBeanDefinition>();
        try {
            for (String classname : regitryBeanClasses) {
                Class<?> beanclazz = Class.forName(classname);
                if (beanclazz.isInterface()) {
                    continue;
                }
                result.add(doCreateBeanDefinition(YJtoLowerCase(beanclazz.getSimpleName()), beanclazz.getName()));
                //2、自定义
                //3、接口注入
                for (Class<?> i : beanclazz.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(), beanclazz.getName()));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private YJBeanDefinition doCreateBeanDefinition(String simpleName, String beanClassName) {
        YJBeanDefinition beanDefinition = new YJBeanDefinition();
        beanDefinition.setFactorBeanName(simpleName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    private String YJtoLowerCase(String simpleName) {
        char[] c = simpleName.toCharArray();
        c[0] += 32;
        return String.valueOf(c);
    }

    public Properties getConfig() {
        return this.properties;
    }
}
