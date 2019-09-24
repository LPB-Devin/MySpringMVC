package com.maven.controller;

import com.maven.annotation.Autowired;

import com.maven.annotation.Controller;
import com.maven.annotation.RequestMapping;
import com.maven.service.TestService;

@Controller
@RequestMapping(value="/test")
public class TestController {

    @Autowired(value = "testService")
    TestService service;

    @RequestMapping(value="/index")
    public String test(){
        System.out.println("=======>"+ service.test());
        return "index";
    }

    public String indexl(){
        return "";
    }

}
