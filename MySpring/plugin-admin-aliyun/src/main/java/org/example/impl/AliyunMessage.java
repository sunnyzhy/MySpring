package org.example.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.MessagePlugin;

/**
 * @author zhy
 * @date 2024/7/22 16:00
 */
public class AliyunMessage<T> implements MessagePlugin<T> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean send(T message) {
        A2.print();
        new A3().print();
        try {
            System.out.println("test aliyun");
            System.out.println("Aliyun send message: " + objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
//
//    @Override
//    public boolean supports(String integer) {
//        return integer.equals("0");
//    }
}
