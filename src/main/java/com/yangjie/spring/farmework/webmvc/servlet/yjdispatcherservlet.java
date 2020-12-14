package com.yangjie.spring.farmework.webmvc.servlet;

import com.yangjie.spring.farmework.annoation.YJAutowired;
import com.yangjie.spring.farmework.annoation.YJController;
import com.yangjie.spring.farmework.annoation.YJRequestMapping;
import com.yangjie.spring.farmework.annoation.YJService;
import com.yangjie.spring.farmework.context.YJApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//手写spring了 看了tom老师的视频 自己练习
public class yjdispatcherservlet extends HttpServlet {
    //存蓄配置文件里面扫描出来的类
    List<String> className = new ArrayList<String>();
    //ioc容器
    Map<String, Object> iocmap = new HashMap<String, Object>();
    //handlerMapping容器
    Map<String, Method> handlerMapping = new HashMap<String, Method>();
    private YJApplicationContext applicationContext;
    private Properties properties = new Properties();

    private List<YJHandlerMapping> handlerMappings = new ArrayList<YJHandlerMapping>();

    private Map<YJHandlerMapping, YJHandlerAdapter> handlerAdapters = new HashMap<YJHandlerMapping, YJHandlerAdapter>();

    private List<YJViewResolver> viewResolvers = new ArrayList<YJViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6、委派,根据URL去找到一个对应的Method并通过response返回
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req, resp, new YJModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, YJModelAndView yjModelAndView) throws Exception{
        if(null == yjModelAndView){return;}
        if(this.viewResolvers.isEmpty()){return;}

                for (YJViewResolver viewResolver : this.viewResolvers) {
            YJView view = viewResolver.resolveViewName(yjModelAndView.getViewName());
            //直接往浏览器输出
            view.render(yjModelAndView.getModel(),req,resp);
            return;
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //完成了对HandlerMapping的封装
        //完成了对方法返回值的封装ModelAndView

        //1、通过URL获得一个HandlerMapping
        YJHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req, resp, new YJModelAndView("404"));
            return;
        }

        //2、根据一个HandlerMaping获得一个HandlerAdapter
        YJHandlerAdapter ha = getHandlerAdapter(handler);

        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        YJModelAndView mv = ha.handler(req, resp, handler);

        // 就把ModelAndView变成一个ViewResolver
        processDispatchResult(req, resp, mv);

    }

    private YJHandlerAdapter getHandlerAdapter(YJHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return null;}
        return this.handlerAdapters.get(handler);
    }

    private YJHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (YJHandlerMapping mapping : handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return mapping;
        }
        return null;
    }

    //配置文件一开始是从ServletConfig 里面取的
    @Override
    public void init(ServletConfig config) throws ServletException {

        applicationContext = new YJApplicationContext(config.getInitParameter("contextConfigLocation"));
        //扫描配置文件 读取web.xml里面的标签
//        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //读取配置文件相关类信息
//        doScanner(properties.getProperty("package"));
        //初始化IOC容器 将刚刚从配置文件里面扫描到的类进行实例化
//        doInstance();
        //DI 依赖注入
//        doAutowired();
        //5、初始化HandlerMapping
//        doInitHandlerMapping();

        //初始化九大组件
        initStrategies(applicationContext);
    }

    private void initStrategies(YJApplicationContext context) {

        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapter(context);
        //初始化视图转换器
        initViewResolvers(context);
    }

    private void initViewResolvers(YJApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new YJViewResolver(templateRoot));
        }

    }

    private void initHandlerAdapter(YJApplicationContext context) {
        for (YJHandlerMapping handlerMapping : handlerMappings) {
            handlerAdapters.put(handlerMapping, new YJHandlerAdapter());
        }

    }

    private void initHandlerMappings(YJApplicationContext context) {
        if (this.applicationContext.getBeanDefinitionCount() == 0) {
            return;
        }

        for (String beanName : this.applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if (!clazz.isAnnotationPresent(YJController.class)) {
                continue;
            }

            //相当于提取 class上配置的url
            String baseUrl = "";
            if (clazz.isAnnotationPresent(YJRequestMapping.class)) {
                YJRequestMapping requestMapping = clazz.getAnnotation(YJRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取public的方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(YJRequestMapping.class)) {
                    continue;
                }
                //提取每个方法上面配置的url
                YJRequestMapping requestMapping = method.getAnnotation(YJRequestMapping.class);

                // //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                //handlerMapping.put(url,method);
                handlerMappings.add(new YJHandlerMapping(pattern, instance, method));
                System.out.println("Mapped : " + regex + "," + method+"instance"+instance);
            }

        }
    }

    private void doAutowired() {
        if (iocmap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : iocmap.entrySet()) {
            //取出实体类里面所有的字段 private public 所有类型都取出来
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(YJAutowired.class)) {
                    continue;
                }

                //得到beanname 如果没有指定beanname 就用默认类型注入
                String beanname = field.getAnnotation(YJAutowired.class).value();
                if (beanname.equals("")) {
                    beanname = field.getType().getName();
                }
                //暴力访问
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), iocmap.get(beanname));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    private void doInstance() {
        if (className.isEmpty()) {
            return;
        }

        //这个位置。tom老师存入集合的时候用的getSimpleName 判断的时候用的getName 有问题。
        //次问题想明白了。getName 是针对接口的。
        //遍历classname
        for (String classname : className) {
            try {
                Class<?> clazz = Class.forName(classname);

                //判断带了注解的类才实例化
                if (clazz.isAnnotationPresent(YJController.class)) {
                    //得到beanname  首字母小写
                    String beanname = YJtoLowerCase(clazz.getSimpleName());
                    iocmap.put(beanname, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(YJService.class)) {
                    //如果@Service没有给名字就用默认的首字母小写用于beanname
                    YJService yjService = clazz.getAnnotation(YJService.class);
                    //一个接口有多个实现类 就自己取一个名字区别 自己理解如果这个注解自己起了名字就用起的名字
                    String beanname = yjService.value();
                    if (beanname.equals("")) {
                        beanname = YJtoLowerCase(clazz.getSimpleName());
                    }

                    iocmap.put(beanname, clazz.newInstance());

                    //如果有多个实现类就报错
                    for (Class<?> c : clazz.getInterfaces()) {
                        if (iocmap.containsKey(c.getName())) {
                            throw new Exception("the" + c.getName() + "is exists");
                        }
                        //一定要把接口装进去。。而不是放beanname。。。。
                        iocmap.put(c.getName(), clazz.newInstance());
                    }
                } else {
                    continue;//启动的不实例化
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private String YJtoLowerCase(String simpleName) {
        char[] c = simpleName.toCharArray();
        c[0] += 32;
        return String.valueOf(c);
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
                className.add(classname);
            }
        }

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


}
