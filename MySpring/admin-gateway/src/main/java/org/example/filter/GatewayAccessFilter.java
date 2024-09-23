package org.example.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ApplicationConfig;
import org.example.util.RedisUtil;
import org.example.util.RequestParamUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author zhy
 * @date 2024/9/23 10:21
 */
@Component
@Slf4j
public class GatewayAccessFilter implements GlobalFilter, Ordered {
    private final CodecConfigurer codecConfigurer;
    private final ObjectMapper objectMapper;
    private final RedisUtil redisUtil;
    private final ApplicationConfig applicationConfig;

    public GatewayAccessFilter(CodecConfigurer codecConfigurer, ObjectMapper objectMapper, RedisUtil redisUtil, ApplicationConfig applicationConfig) {
        this.codecConfigurer = codecConfigurer;
        this.objectMapper = objectMapper;
        this.redisUtil = redisUtil;
        this.applicationConfig = applicationConfig;
    }

    @PostConstruct
    public void init() {
        RequestParamUtil.set(applicationConfig, objectMapper, redisUtil, codecConfigurer);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        MediaType contentType = headers.getContentType();
        HttpMethod method = request.getMethod();
        if (method.equals(HttpMethod.GET)) {
            return RequestParamUtil.readGetData(exchange, chain);
        } else {
            // 如果contentType为空，就说明requestBody为空，参数可能附带在url里
            // 一种常见的情况就是前端使用 post + params 的方式请求
            if (contentType == null) {
                return RequestParamUtil.readGetData(exchange, chain);
            }
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                return RequestParamUtil.readJsonData(exchange, chain);
            }
            if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                return RequestParamUtil.readFormData(exchange, chain);
            }
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                return RequestParamUtil.readMultipartData(exchange, chain);
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
