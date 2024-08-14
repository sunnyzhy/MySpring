package org.example.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.MessagePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @date 2024/7/22 16:00
 */
@Component
public class TencentMessage<T> implements MessagePlugin<T> {
    @Autowired
    private ApplicationContext applicationContext;
    private final TencentPrint tencentPrint;
    private final A0 a0;
    private ObjectMapper objectMapper=new ObjectMapper();

    public TencentMessage(TencentPrint tencentPrint, A0 a0) {
        this.tencentPrint = tencentPrint;
        this.a0 = a0;
    }

    @Override
    public boolean send(T message) {
        A2.print();
        new A3().print();
        try {
            System.out.println("tencent class send");
            String s = "Tencent send message: "+objectMapper.writeValueAsString(message);
            tencentPrint.print(s);
        } catch (JsonProcessingException e) {
            return false;
        }
        return true;
    }

//    @Override
//    public boolean supports(String integer) {
//        return integer.equals("1");
//    }
@PostConstruct
public void postConstruct(){
    System.out.println("TencentMessage加载成功");
}
    @PreDestroy
    public void preDestroy() {
        System.out.println("TencentMessage卸载成功");
    }
}
