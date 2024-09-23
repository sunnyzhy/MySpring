package org.example.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author zhy
 * @date 2024/9/9 9:51
 */
@Slf4j
public class HttpUtil {
    private static RestTemplate restTemplate;

    static {
        restTemplate = new RestTemplate();
    }

    public static ResponseEntity<String> request(HttpEntityVo entityVo) {
        // 初始化url
        String url = entityVo.getUrl();
        // 初始化HttpMethod
        String method = entityVo.getMethod();
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(method)) {
            return ResponseEntity.ok("参数错误");
        }
        method = method.toUpperCase();
        // 初始化HttpHeaders
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
                // 初始化HttpBody
                String body = entityVo.getBody();
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                entity = new HttpEntity(body, headers);
            }
            if (entity == null) {
                return ResponseEntity.ok("HttpMethod不允许");
            }
            // rest调用
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
    }
}
