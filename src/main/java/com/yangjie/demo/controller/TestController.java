package com.yangjie.demo.controller;

import com.yangjie.demo.service.TestService;
import com.yangjie.spring.farmework.annoation.YJAutowired;
import com.yangjie.spring.farmework.annoation.YJController;
import com.yangjie.spring.farmework.annoation.YJRequestMapping;
import com.yangjie.spring.farmework.annoation.YJRequestParam;
import com.yangjie.spring.farmework.webmvc.servlet.YJModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@YJController
public class TestController {

    @YJAutowired
    private TestService testService;

    @YJRequestMapping("/hello")
    public YJModelAndView hello(HttpServletRequest req, HttpServletResponse response, @YJRequestParam("name") String name, @YJRequestParam("sex") String sex) {
        try {
//            response.getWriter().write(testService.hello()+"name:"+name+"sex:"+sex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out(response, testService.hello() + "name:" + name + "sex:" + sex);
    }

    @YJRequestMapping("/first.html")
    public YJModelAndView query(@YJRequestParam("teacher") String teacher) {
        String result = testService.hello();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new YJModelAndView("first.html", model);
    }

    private YJModelAndView out(HttpServletResponse resp, String str) {
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
