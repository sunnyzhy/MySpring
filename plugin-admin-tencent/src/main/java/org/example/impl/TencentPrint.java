package org.example.impl;

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
}
