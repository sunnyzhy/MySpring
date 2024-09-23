package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ApplicationConfig;
import org.example.config.ConstantSet;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.DigestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR;

/**
 * @author zhy
 * @date 2024/9/23 10:23
 */
@Slf4j
public class RequestParamUtil {
    private static ApplicationConfig applicationConfig;
    private static ObjectMapper objectMapper;
    private static RedisUtil redisUtil;
    private static List<HttpMessageReader<?>> messageReaders;
    public static final String CACHE_REQUEST_BODY_OBJECT_ATTR = "adminCachedRequestBodyObject";

    public static void set(ApplicationConfig config, ObjectMapper mapper, RedisUtil redis, CodecConfigurer codecConfigurer) {
        applicationConfig = config;
        objectMapper = mapper;
        redisUtil = redis;
        messageReaders = codecConfigurer.getReaders();
    }

    /**
     * ReadGetData
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readGetData(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean check = checkSign(exchange, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                MultiValueMap<String, String> queryParams = request.getQueryParams();
                for (Map.Entry<String, String> entry : queryParams.toSingleValueMap().entrySet()) {
                    String value = encode(entry.getValue());
                    // 过滤空值
                    if (isEmpty(value)) {
                        continue;
                    }
                    sortedMap.put(entry.getKey(), value);
                }
            }
        });
        if (!check) {
            return setFailedRequest(exchange, ConstantSet.SIGN_ERROR.getName(), HttpStatus.FORBIDDEN);
        }
        return chain.filter(exchange);
    }

    /**
     * ReadJsonBody
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readJsonData(ServerWebExchange exchange, GatewayFilterChain chain) {
        Mono<Void> mono = doReadData(exchange, chain, messageReaders, new ParameterizedTypeReference<String>() {
        }, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                // 封装params
                MultiValueMap<String, String> queryParams = request.getQueryParams();
                for (Map.Entry<String, String> entry : queryParams.toSingleValueMap().entrySet()) {
                    String value = encode(entry.getValue());
                    // 过滤空值
                    if (isEmpty(value)) {
                        continue;
                    }
                    sortedMap.put(entry.getKey(), value);
                }
                // 封装body
                String cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_ATTR);
                // 过滤空值
                if (isEmpty(cachedBody)) {
                    return;
                }
                sortedMap.put("", cachedBody);
            }
        });
        return mono;
    }

    /**
     * ReadFormData
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readFormData(ServerWebExchange exchange, GatewayFilterChain chain) {
        Mono<Void> mono = doReadData(exchange, chain, messageReaders, new ParameterizedTypeReference<MultiValueMap<String, String>>() {
        }, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                MultiValueMap<String, String> cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_ATTR);
                if (cachedBody == null) {
                    return;
                }
                for (Map.Entry<String, String> entry : cachedBody.toSingleValueMap().entrySet()) {
                    String value = encode(entry.getValue());
                    // 过滤空值
                    if (isEmpty(value)) {
                        continue;
                    }
                    sortedMap.put(entry.getKey(), value);
                }
            }
        });
        return mono;
    }

    /**
     * ReadMultipartData
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readMultipartData(ServerWebExchange exchange, GatewayFilterChain chain) {
        Mono<Void> mono = doReadData(exchange, chain, messageReaders, new ParameterizedTypeReference<MultiValueMap<String, Part>>() {
        }, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                MultiValueMap<String, Part> cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_ATTR);
                if (cachedBody == null) {
                    return;
                }
                for (Map.Entry<String, Part> entry : cachedBody.toSingleValueMap().entrySet()) {
                    Part part = entry.getValue();
                    String value = null;
                    if (part instanceof FilePart) {
                        value = ((FilePart) part).filename();
                    } else {
                        value = ((FormFieldPart) part).value();
                    }
                    value = encode(value);
                    // 过滤空值
                    if (isEmpty(value)) {
                        continue;
                    }
                    sortedMap.put(entry.getKey(), value);
                }
            }
        });
        return mono;
    }

    private static <T> Mono<Void> doReadData(ServerWebExchange exchange, GatewayFilterChain chain, List<HttpMessageReader<?>> messageReaders, ParameterizedTypeReference<T> typeReference, AnalyseData analyseData) {
        Mono<Void> mono = ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, (serverHttpRequest) -> {
            final ServerRequest serverRequest = ServerRequest
                    .create(exchange.mutate().request(serverHttpRequest).build(), messageReaders);
            return serverRequest.bodyToMono(typeReference)
                    .doOnNext(objectValue -> {
                        // 使用自定义key，直接修改框架内部属性如（ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR）会引起不确定的行为（内存泄漏）
                        exchange.getAttributes().put(CACHE_REQUEST_BODY_OBJECT_ATTR, objectValue);
                    }).then(Mono.defer(() -> {
                        boolean check = checkSign(exchange, analyseData);
                        if (!check) {
                            return setFailedRequest(exchange, ConstantSet.SIGN_ERROR.getName(), HttpStatus.FORBIDDEN);
                        }
                        ServerHttpRequest cachedRequest = exchange
                                .getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
                        exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
                        return chain.filter(exchange.mutate().request(cachedRequest).build());
                    })).doFinally(signalType -> {
                        // 释放资源
                        exchange.getAttributes().remove(CACHE_REQUEST_BODY_OBJECT_ATTR);
                    });
        });

        return mono;
    }

    private static boolean checkSign(ServerWebExchange exchange, AnalyseData analyseData) {
        ServerHttpRequest request = exchange.getRequest();
        // 不需要验签
        if (!applicationConfig.enable) {
            return true;
        }
        // 验签
        HttpHeaders headers = request.getHeaders();
        String requestSign = headers.getFirst(ConstantSet.SIGN_SIGN.getName());
        String requestTimestamp = headers.getFirst(ConstantSet.SIGN_TIMESTAMP.getName());
        String requestNonce = headers.getFirst(ConstantSet.SIGN_NONCE.getName());
        if (StringUtils.isEmpty(requestTimestamp) || StringUtils.isEmpty(requestNonce) || StringUtils.isEmpty(requestSign)) {
            return false;
        }
        // 判断时间戳，如果客户端与服务端的时间戳差值大于指定的有效期，就说明客户端的时间戳无效
        long timestamp = System.currentTimeMillis() - Long.parseLong(requestTimestamp);
        timestamp = Math.abs(timestamp);
        if (timestamp > applicationConfig.expire * 1000) {
            return false;
        }
        // 判断nonce，如果nonce已经存在，就说明是重复请求，不允许请求通过
        String key = String.format(ConstantSet.SIGN_REDIS_HASH_KEY_FORMAT.getName(), requestNonce);
        Boolean value = redisUtil.hasKey(key);
        if (value) {
            return false;
        }
        // 如果nonce不存在，就允许请求通过，并把nonce保存到缓存且设置有效期
        // 注：不要手动删除，而是到期后由缓存自动删除，这样才能实现某一时间段内只允许同一个url请求一次的目的
        redisUtil.set(key, requestNonce, applicationConfig.expire + 60, TimeUnit.SECONDS);
        // 判断签名
        StringBuilder stringBuilder = new StringBuilder();
        SortedMap<String, String> sortedMap = new TreeMap<>();
        if (analyseData != null) {
            analyseData.execute(request, sortedMap);
        }
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            if (StringUtils.isEmpty(entry.getKey())) {
                stringBuilder.append(entry.getValue());
                stringBuilder.append(ConstantSet.AND.getName());
            } else {
                stringBuilder.append(entry.getKey());
                stringBuilder.append(ConstantSet.EQUAL.getName());
//                if (entry.getValue() != null) {
//                    stringBuilder.append(encode(entry.getValue()));
//                }
                stringBuilder.append(entry.getValue());
                stringBuilder.append(ConstantSet.AND.getName());
            }
        }
        stringBuilder.append(ConstantSet.SIGN_TIMESTAMP.getName());
        stringBuilder.append(ConstantSet.EQUAL.getName());
        stringBuilder.append(requestTimestamp);
        stringBuilder.append(ConstantSet.AND.getName());
        stringBuilder.append(ConstantSet.SIGN_NONCE.getName());
        stringBuilder.append(ConstantSet.EQUAL.getName());
        stringBuilder.append(requestNonce);
        stringBuilder.append(ConstantSet.AND.getName());
        stringBuilder.append(ConstantSet.SIGN_SECRET.getName());
        stringBuilder.append(ConstantSet.EQUAL.getName());
        stringBuilder.append(applicationConfig.secret);
        log.debug(stringBuilder.toString());
        String sign = DigestUtils.md5DigestAsHex(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        sign = sign.toUpperCase();
        return sign.equals(requestSign);
    }

    private static boolean isEmpty(String content) {
        if (StringUtils.isEmpty(content)) {
            return true;
        }
        if (content.trim().equalsIgnoreCase(ConstantSet.UNDEFINED.getName())) {
            return true;
        }
        if (content.trim().equalsIgnoreCase(ConstantSet.NULL.getName())) {
            return true;
        }
        if ((content.contains("[") && content.contains("]")) || (content.contains("{") && content.contains("}"))) {
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(content);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                return false;
            }
            return rootNode.isEmpty();
        }
        return false;
    }

    private static String encode(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            value = URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
        if (value.indexOf('+') >= 0) {
            value = value.replace("+", "%20");
        }
        return value;
    }

    private static Mono<Void> setFailedRequest(ServerWebExchange exchange, String body, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", MediaType.TEXT_HTML_VALUE);
        response.setStatusCode(status);
        ResponseEntity<String> failed = ResponseEntity.badRequest().body(body);
        byte[] responseByte = JsonUtil.toBytes(failed);
        DataBuffer buffer = response.bufferFactory().wrap(responseByte);
        return response.writeWith(Mono.just(buffer));
    }

    interface AnalyseData {
        void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap);
    }
}
