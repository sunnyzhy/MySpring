package org.example.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @date 2024/8/6 17:45
 */
@Component
public class A1 {
    private final A0 a0;

    public A1(A0 a0) {
        this.a0 = a0;
    }
    @PostConstruct
    public void postConstruct(){
        System.out.println("A1加载成功");
    }
    @PreDestroy
    public void preDestroy() {
        System.out.println("A1卸载成功");
    }
}
