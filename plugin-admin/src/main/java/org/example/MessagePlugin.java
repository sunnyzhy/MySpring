package org.example;

/**
 * @author zhy
 * @date 2024/7/22 15:57
 */
public interface MessagePlugin<T> {
    boolean send(T message);
}
