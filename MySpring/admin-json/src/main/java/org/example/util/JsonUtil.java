package org.example.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhy
 * @date 2021/6/22 13:46
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 将对象转为JSON字符串
     *
     * @param t
     * @return
     */
    public static <T> String toString(T t) {
        if (t == null) {
            return "";
        }
        try {
            return t instanceof String ? (String) t : objectMapper.writeValueAsString(t);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return "";
        }
    }

    /**
     * 将对象转为字节流
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> byte[] toBytes(T t) {
        if (t == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(t);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return new byte[]{};
        }
    }

    /**
     * 将Object对象转为具体类型的简单对象
     *
     * @param s
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toObject(String s, Class<T> clazz) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        try {
            return objectMapper.readValue(s, clazz);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将字节数组转为具体类型的简单对象
     *
     * @param buffer
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toObject(byte[] buffer, Class<T> clazz) {
        if (buffer == null || buffer.length == 0) {
            return null;
        }
        try {
            String s = new String(buffer, Charset.forName("utf-8"));
            return objectMapper.readValue(s, clazz);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将Object对象转为具体类型的简单对象
     *
     * @param object
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toObject(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(object, clazz);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将Object对象转为具体类型的复杂对象(List/Map)
     *
     * @param object
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T toObject(Object object, TypeReference<T> typeReference) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(object, typeReference);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将Object转为成员是简单对象的List
     *
     * @param object
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(Object object, Class<T> clazz) {
        if (object == null) {
            return new ArrayList<>();
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return objectMapper.convertValue(object, listType);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 将Object转为成员是简单对象的Set
     *
     * @param object
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Set<T> toSet(Object object, Class<T> clazz) {
        if (object == null) {
            return new HashSet<>();
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(HashSet.class, clazz);
            return objectMapper.convertValue(object, listType);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return new HashSet<>();
        }
    }

    /**
     * 将Object转为成员是简单对象的Map
     *
     * @param object
     * @param keyClazz
     * @param valueClazz
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(Object object, Class<K> keyClazz, Class<V> valueClazz) {
        if (object == null) {
            return new HashMap<>();
        }
        try {
            MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, keyClazz, valueClazz);
            return objectMapper.convertValue(object, mapType);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
