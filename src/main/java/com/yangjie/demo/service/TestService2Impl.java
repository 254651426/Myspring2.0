package com.yangjie.demo.service;

import com.yangjie.spring.farmework.annoation.YJService;

@YJService
public class TestService2Impl implements TestService {
    @Override
    public String hello() {
        return "nihao2";
    }
}
