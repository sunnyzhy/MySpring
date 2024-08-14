package org.example.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @date 2024/7/30 18:22
 */
@Component
public class TencentPrint {
    private final A1 a1;

    public TencentPrint(A1 a1) {
        this.a1 = a1;
    }

    public void print(String s){
        System.out.println(s);
    }
    @PostConstruct
    public void postConstruct(){
        System.out.println("TencentPrint加载成功");
    }
    @PreDestroy
    public void preDestroy() {
        System.out.println("TencentPrint卸载成功");
    }
}
