package org.example.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.MessagePlugin;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @date 2024/7/22 16:00
 */
@Component
public class TencentMessage<T> implements MessagePlugin<T> {
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
}
