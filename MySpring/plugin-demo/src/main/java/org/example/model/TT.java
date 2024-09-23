//package org.example.model;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.client.RestTemplateBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.ClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
///**
// * @author zhy
// * @date 2024/8/30 18:02
// */
//@Configuration
//public class TT {
//    @Autowired
//    //RestTemplateBuilder
//    private RestTemplateBuilder builder;
//    // 使用RestTemplateBuilder来实例化RestTemplate对象，spring默认已经注入了RestTemplateBuilder实例
//    @Bean
//    public RestTemplate restTemplate() {
//        return builder.build();
//    }
//
//}
