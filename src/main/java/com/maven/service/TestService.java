package com.maven.service;


import com.maven.annotation.Service;

@Service(value = "testService")
public class TestService {
    public String test(){
        return "TestService.test()";
    }
}
