package org.example.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhy
 * @date 2024/9/9 9:51
 */
@Slf4j
public class HttpUtil {
    private static ConcurrentHashMap<String, RestTemplate> restTemplateMap = new ConcurrentHashMap<>();

    public static ResponseEntity<String> request(HttpEntityVo entityVo) {
        String key = String.format("%s:%s", entityVo.getConnectTimeOut(), entityVo.getReadTimeout());
        if (!restTemplateMap.containsKey(key)) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(entityVo.getConnectTimeOut());
            factory.setReadTimeout(entityVo.getReadTimeout());
            RestTemplate restTemplate = new RestTemplate(factory);
            restTemplateMap.put(key, restTemplate);
        }
        RestTemplate restTemplate = restTemplateMap.get(key);
        String url = entityVo.getUrl();
        String method = entityVo.getMethod();
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(method)) {
            return ResponseEntity.ok("参数错误");
        }
        method = method.toUpperCase();
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, Object> entry : entityVo.getHeaders().entrySet()) {
            headers.add(entry.getKey(), entry.getValue().toString());
        }
        HttpEntity entity = null;
        ResponseEntity<String> responseEntity = null;
        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method);
            if (httpMethod.equals(HttpMethod.GET)) {
                headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                entity = new HttpEntity(headers);
            } else if (httpMethod.equals(HttpMethod.POST) || httpMethod.equals(HttpMethod.PUT) || httpMethod.equals(HttpMethod.DELETE)) {
                String body = entityVo.getBody();
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                entity = new HttpEntity(body, headers);
            }
            if (entity == null) {
                return ResponseEntity.ok("不支持的HttpMethod");
            }
            url = URLDecoder.decode(url, StandardCharsets.UTF_8);
            responseEntity = restTemplate.exchange(url, httpMethod, entity, String.class);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ResponseEntity.ok(ex.getMessage());
        }
        return responseEntity;
    }

    @Data
    public static class HttpEntityVo {
        private String url;
        private String method;
        private Map<String, Object> headers = new HashMap<>();
        private String body;
        private Integer connectTimeOut = 15000;
        private Integer readTimeout = 60000;
    }
}
