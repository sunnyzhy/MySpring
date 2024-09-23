package org.example.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.MessagePlugin;
import org.example.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @date 2024/7/22 16:00
 */
@Component
public class TencentMessage<T> implements MessagePlugin<T> {
    @Autowired
    private Conf conf;
    private final TencentPrint tencentPrint;
    private final A0 a0;
    private ObjectMapper objectMapper = new ObjectMapper();

    public TencentMessage(TencentPrint tencentPrint, A0 a0) {
        this.tencentPrint = tencentPrint;
        this.a0 = a0;
    }

    @Override
    public boolean send(T message) {
        A2.print();
        new A3().print();
        System.out.println(conf.runMode);
        try {
            HttpUtil.HttpEntityVo vo = new HttpUtil.HttpEntityVo();
            vo.setUrl("http://20.0.0.48:8702/event/type/all");
            vo.setMethod("get");
            vo.getHeaders().put("tenantId","b8e83a519a41418583bd847e8b108f22");
            vo.getHeaders().put("userId",2);
            vo.getHeaders().put("userType",2);
            vo.getHeaders().put("userName","admin");
            ResponseEntity<String> request = HttpUtil.request(vo);
            System.out.println(request.getBody());

            System.out.println("tencent class send");
            String s = "Tencent send message: " + objectMapper.writeValueAsString(message);
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
    public void postConstruct() {
        System.out.println("TencentMessage加载成功");
    }

    @PreDestroy
    public void preDestroy() {
        System.out.println("TencentMessage卸载成功");
    }
}
