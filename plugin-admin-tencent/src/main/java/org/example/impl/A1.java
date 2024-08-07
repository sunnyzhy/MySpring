package org.example.impl;

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
}
