package org.example.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @date 2024/8/6 17:45
 */
@Component
public class A0 {
    @Autowired
    private ApplicationContext applicationContext;
    @PostConstruct
    public void postConstruct(){
        System.out.println("A0加载成功");
    }
    @PreDestroy
    public void preDestroy() {
        System.out.println("A0卸载成功");
    }
}
