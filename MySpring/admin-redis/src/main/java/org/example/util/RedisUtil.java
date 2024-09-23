package org.example.util;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zhy
 * @date 2024/9/23 10:57
 */
@Component
@Slf4j
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private long count = 2000L;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public Boolean hasKey(String key) {
        return hasKey(redisTemplate, key);
    }

    public Boolean hasKey(RedisTemplate<String, Object> redisTemplate, String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
            return false;
        }
    }

    /**
     * 判断hashKey是否存在
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Boolean hasHashKey(String key, Object hashKey) {
        return hasHashKey(redisTemplate, key, hashKey);
    }

    public Boolean hasHashKey(RedisTemplate<String, Object> redisTemplate, String key, Object hashKey) {
        try {
            return redisTemplate.opsForHash().hasKey(key, hashKey);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hashKey), ex);
        }
        return false;
    }

    /**
     * 获取键对应的值
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        return get(redisTemplate, key);
    }

    public Object get(RedisTemplate<String, Object> redisTemplate, String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
            return null;
        }
    }

    /**
     * 获取键对应的值
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(String key, Class<T> clazz) {
        return get(redisTemplate, key, clazz);
    }

    public <T> T get(RedisTemplate<String, Object> redisTemplate, String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value == null ? null : JsonUtil.toObject(value, clazz);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        return get(redisTemplate, key, typeReference);
    }

    public <T> T get(RedisTemplate<String, Object> redisTemplate, String key, TypeReference<T> typeReference) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value == null ? null : JsonUtil.toObject(value, typeReference);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
            return null;
        }
    }

    /**
     * 获取键列表所对应的值列表
     *
     * @param keys
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> multiGet(List<String> keys, Class<T> clazz, boolean... excludeNull) {
        return multiGet(redisTemplate, keys, clazz, excludeNull);
    }

    public <T> List<T> multiGet(RedisTemplate<String, Object> redisTemplate, List<String> keys, Class<T> clazz, boolean... excludeNull) {
        List<T> valueList = new ArrayList<>();
        List<String> keyList = new ArrayList<>();
        for (String k : keys) {
            keyList.add(k);
            if (keyList.size() == count) {
                doMultiGet(redisTemplate, keyList, valueList, clazz, excludeNull);
            }
        }
        if (!keyList.isEmpty()) {
            doMultiGet(redisTemplate, keyList, valueList, clazz, excludeNull);
        }
        return valueList;
    }

    /**
     * 内部封装的批量方法，用于初始化值列表
     *
     * @param keyList
     * @param valueList
     * @param clazz
     * @param <T>
     */
    private <T> void doMultiGet(RedisTemplate<String, Object> redisTemplate, List<String> keyList, List<T> valueList, Class<T> clazz, boolean... excludeNull) {
        try {
            List<Object> list = redisTemplate.opsForValue().multiGet(keyList);
            if (list != null && !list.isEmpty()) {
                if (excludeNull.length == 0 || excludeNull[0]) {
                    list.removeIf(Objects::isNull);
                }
                list.forEach(x -> {
                    if (x == null) {
                        valueList.add(null);
                    } else {
                        valueList.add(JsonUtil.toObject(x, clazz));
                    }
                });
            }
            keyList.clear();
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), StringUtils.join(keyList)), ex);
        }
    }

    /**
     * 设置键值
     *
     * @param key
     * @param value
     * @param <T>
     */
    public <T> void set(String key, T value) {
        set(redisTemplate, key, value);
    }

    public <T> void set(RedisTemplate<String, Object> redisTemplate, String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    /**
     * 设置键值
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit
     * @param <T>
     */
    public <T> void set(String key, T value, Long expireTime, TimeUnit timeUnit) {
        set(redisTemplate, key, value, expireTime, timeUnit);
    }

    public <T> void set(RedisTemplate<String, Object> redisTemplate, String key, T value, Long expireTime, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    /**
     * 批量方法，设置键值
     *
     * @param map
     * @param <V>
     */
    public <V> void set(Map<String, V> map) {
        set(redisTemplate, map);
    }

    public <V> void set(RedisTemplate<String, Object> redisTemplate, Map<String, V> map) {
        try {
            redisTemplate.opsForValue().multiSet(map);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), StringUtils.join(map.keySet())), ex);
        }
    }

    public Set<Object> getHashKeys(String key) {
        return getHashKeys(redisTemplate, key);
    }

    public Set<Object> getHashKeys(RedisTemplate<String, Object> redisTemplate, String key) {
        try {
            return redisTemplate.opsForHash().keys(key);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return new HashSet<>();
    }

    public <T> List<T> getHashValues(String key, Class<T> clazz) {
        return getHashValues(redisTemplate, key, clazz);
    }

    public <T> List<T> getHashValues(RedisTemplate<String, Object> redisTemplate, String key, Class<T> clazz) {
        try {
            List<Object> list = redisTemplate.opsForHash().values(key);
            list.removeIf(Objects::isNull);
            if (list != null && !list.isEmpty()) {
                return JsonUtil.toList(list, clazz);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return new ArrayList<>();
    }

    /**
     * 获取哈希键对应的值
     *
     * @param key
     * @param hkey
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getHash(String key, Object hkey, Class<T> clazz) {
        return getHash(redisTemplate, key, hkey, clazz);
    }

    public <T> T getHash(RedisTemplate<String, Object> redisTemplate, String key, Object hkey, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hkey);
            return value == null ? null : JsonUtil.toObject(value, clazz);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hkey), ex);
            return null;
        }
    }

    public <T> T getHash(String key, Object hkey, TypeReference<T> typeReference) {
        return getHash(redisTemplate, key, hkey, typeReference);
    }

    public <T> T getHash(RedisTemplate<String, Object> redisTemplate, String key, Object hkey, TypeReference<T> typeReference) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hkey);
            return value == null ? null : JsonUtil.toObject(value, typeReference);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hkey), ex);
            return null;
        }
    }

    public <T> List<T> multiGetHash(String key, List<Object> hashKeys, Class<T> clazz) {
        return multiGetHash(redisTemplate, key, hashKeys, clazz);
    }

    public <T> List<T> multiGetHash(RedisTemplate<String, Object> redisTemplate, String key, List<Object> hashKeys, Class<T> clazz) {
        try {
            List<Object> list = redisTemplate.opsForHash().multiGet(key, hashKeys);
            list.removeIf(Objects::isNull);
            if (list != null && !list.isEmpty()) {
                return JsonUtil.toList(list, clazz);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, StringUtils.join(hashKeys)), ex);
        }
        return new ArrayList<>();
    }

    public <T> T multiGetHash(String key, List<Object> hashKeys, TypeReference<T> typeReference) {
        return multiGetHash(redisTemplate, key, hashKeys, typeReference);
    }

    public <T> T multiGetHash(RedisTemplate<String, Object> redisTemplate, String key, List<Object> hashKeys, TypeReference<T> typeReference) {
        try {
            List<Object> list = redisTemplate.opsForHash().multiGet(key, hashKeys);
            list.removeIf(Objects::isNull);
            if (list != null && !list.isEmpty()) {
                return JsonUtil.toObject(list, typeReference);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, StringUtils.join(hashKeys)), ex);
        }
        return null;
    }

    /**
     * 获取哈希键对应的值
     *
     * @param key
     * @param keyClazz
     * @param valueClazz
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> Map<K, V> getMapInHash(String key, Class<K> keyClazz, Class<V> valueClazz) {
        return getMapInHash(redisTemplate, key, keyClazz, valueClazz);
    }

    public <K, V> Map<K, V> getMapInHash(RedisTemplate<String, Object> redisTemplate, String key, Class<K> keyClazz, Class<V> valueClazz) {
        try {
            Map<Object, Object> value = redisTemplate.opsForHash().entries(key);
            if (value != null) {
                return JsonUtil.toMap(value, keyClazz, valueClazz);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return new HashMap<>();
    }

    public <V> Map<String, V> getMapInHash(String key, Object hashKey, Class<V> valueClazz) {
        return getMapInHash(redisTemplate, key, hashKey, valueClazz);
    }

    public <V> Map<String, V> getMapInHash(RedisTemplate<String, Object> redisTemplate, String key, Object hashKey, Class<V> valueClazz) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hashKey);
            if (value != null) {
                return JsonUtil.toMap(value, String.class, valueClazz);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hashKey), ex);
        }
        return new HashMap<>();
    }

    public <V> V getMapInHash(String key, Object hashKey, TypeReference<V> typeReference) {
        return getMapInHash(redisTemplate, key, hashKey, typeReference);
    }

    public <V> V getMapInHash(RedisTemplate<String, Object> redisTemplate, String key, Object hashKey, TypeReference<V> typeReference) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hashKey);
            if (value != null) {
                return JsonUtil.toObject(value, typeReference);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hashKey), ex);
        }
        return null;
    }

    public Map<Object, Object> getMapInHash(String key) {
        return getMapInHash(redisTemplate, key);
    }

    public Map<Object, Object> getMapInHash(RedisTemplate<String, Object> redisTemplate, String key) {
        Map<Object, Object> value = new HashMap<>();
        try {
            value.putAll(redisTemplate.opsForHash().entries(key));
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return value;
    }

    public <V> Map<Integer, V> getIntegerMapInHash(String key, Object hashKey, Class<V> valueClazz) {
        return getIntegerMapInHash(redisTemplate, key, hashKey, valueClazz);
    }

    public <V> Map<Integer, V> getIntegerMapInHash(RedisTemplate<String, Object> redisTemplate, String key, Object hashKey, Class<V> valueClazz) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hashKey);
            if (value != null) {
                return JsonUtil.toMap(value, Integer.class, valueClazz);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hashKey), ex);
        }
        return new HashMap<>();
    }

    /**
     * 设置哈希键值
     *
     * @param key
     * @param hKey
     * @param hValue
     * @param <K>
     * @param <V>
     */
    public <K, V> void setHash(String key, K hKey, V hValue) {
        setHash(redisTemplate, key, hKey, hValue);
    }

    public <K, V> void setHash(RedisTemplate<String, Object> redisTemplate, String key, K hKey, V hValue) {
        setHash(redisTemplate, key, hKey, hValue, null, null);
    }

    /**
     * 设置哈希键值
     *
     * @param key
     * @param hKey
     * @param hValue
     * @param expireTime
     * @param timeUnit
     * @param <K>
     * @param <V>
     */
    public <K, V> void setHash(String key, K hKey, V hValue, Long expireTime, TimeUnit timeUnit) {
        setHash(redisTemplate, key, hKey, hValue, expireTime, timeUnit);
    }

    public <K, V> void setHash(RedisTemplate<String, Object> redisTemplate, String key, K hKey, V hValue, Long expireTime, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().put(key, hKey, hValue);
            if (expireTime != null && timeUnit != null) {
                redisTemplate.expire(key, expireTime, timeUnit);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hKey), ex);
        }
    }

    /**
     * 批量方法，设置哈希键值
     *
     * @param key
     * @param map
     * @param <K>
     * @param <V>
     */
    public <K, V> void setHash(String key, Map<K, V> map) {
        setHash(redisTemplate, key, map);
    }

    public <K, V> void setHash(RedisTemplate<String, Object> redisTemplate, String key, Map<K, V> map) {
        setHash(redisTemplate, key, map, null, null);
    }

    /**
     * 批量方法，设置哈希键值
     *
     * @param key
     * @param map
     * @param expireTime
     * @param timeUnit
     * @param <K>
     * @param <V>
     */
    public <K, V> void setHash(String key, Map<K, V> map, Long expireTime, TimeUnit timeUnit) {
        setHash(redisTemplate, key, map, expireTime, timeUnit);
    }

    public <K, V> void setHash(RedisTemplate<String, Object> redisTemplate, String key, Map<K, V> map, Long expireTime, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (expireTime != null && timeUnit != null) {
                redisTemplate.expire(key, expireTime, timeUnit);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    /**
     * 删除哈希键值
     *
     * @param key
     * @param hkey
     */
    public void deleteHash(String key, Object hkey) {
        deleteHash(redisTemplate, key, hkey);
    }

    public void deleteHash(RedisTemplate<String, Object> redisTemplate, String key, Object hkey) {
        try {
            redisTemplate.opsForHash().delete(key, hkey);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s; hashKey:%s", ex.getMessage(), key, hkey), ex);
        }
    }

    /**
     * 删除键值
     *
     * @param key
     */
    public void delete(String key) {
        delete(redisTemplate, key);
    }

    public void delete(RedisTemplate<String, Object> redisTemplate, String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    /**
     * 批量方法，删除键值
     *
     * @param keys
     */
    public void delete(List<String> keys) {
        delete(redisTemplate, keys);
    }

    public void delete(RedisTemplate<String, Object> redisTemplate, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        Set<String> hashSet = new HashSet<>(keys);
        delete(redisTemplate, hashSet);
    }

    /**
     * 删除hash键值
     *
     * @param key
     */
    public void deleteHash(String key) {
        deleteHash(redisTemplate, key);
    }

    public void deleteHash(RedisTemplate<String, Object> redisTemplate, String key) {
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        if (keys.isEmpty()) {
            return;
        }
        redisTemplate.opsForHash().delete(key, keys.toArray());
    }

    /**
     * 删除hash键值
     *
     * @param key
     */
    public void deleteHash(String key, List<Object> hashKey) {
        deleteHash(redisTemplate, key, hashKey);
    }

    public void deleteHash(RedisTemplate<String, Object> redisTemplate, String key, List<Object> hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey.toArray());
    }


    /**
     * 删除含有通配符的键列表
     *
     * @param pattern
     */
    public void deleteByPattern(String pattern) {
        deleteByPattern(redisTemplate, pattern);
    }

    public void deleteByPattern(RedisTemplate<String, Object> redisTemplate, String pattern) {
        scan(redisTemplate, pattern, new RedisScanHandler() {
            @Override
            public void doHandle(RedisTemplate<String, Object> redisTemplate, Set<String> keys) {
                delete(redisTemplate, keys);
            }
        });
    }

    private void scan(RedisTemplate<String, Object> redisTemplate, String pattern, RedisScanHandler redisScanHandler) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count)
                .build();
        redisTemplate.execute(new RedisCallback<Set<String>>() {
            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<String> keys = new HashSet<>();
                Cursor<byte[]> cursor = connection.scan(options);
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                    if (keys.size() == count) {
                        redisScanHandler.doHandle(redisTemplate, keys);
                        keys.clear();
                    }
                }
                if (keys.size() > 0) {
                    redisScanHandler.doHandle(redisTemplate, keys);
                    keys.clear();
                }
                return keys;
            }
        });
    }

    /**
     * 内部封装的批量方法，防止键列表过大引起OOM
     *
     * @param keys
     */
    private void delete(RedisTemplate<String, Object> redisTemplate, Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        List<String> list = new ArrayList<>();
        try {
            for (String k : keys) {
                list.add(k);
                if (list.size() == count) {
                    redisTemplate.delete(list);
                    list.clear();
                }
            }
            if (!list.isEmpty()) {
                redisTemplate.delete(list);
                list.clear();
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), StringUtils.join(list)), ex);
        }
    }

    /**
     * 对存储在指定键的数值执行原子加 n 的操作
     *
     * @param key
     * @param step
     * @return
     */
    public Long increment(String key, long step) {
        return increment(redisTemplate, key, step);
    }

    public Long increment(RedisTemplate<String, Object> redisTemplate, String key, long step) {
        try {
            return redisTemplate.opsForValue().increment(key, step);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return 0L;
    }

    /**
     * 设置字符串的键值
     *
     * @param key
     * @param value
     */
    public void setString(String key, String value) {
        setString(stringRedisTemplate, key, value);
    }

    public void setString(StringRedisTemplate stringRedisTemplate, String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    /**
     * 设置字符串的键值
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit
     */
    public void setString(String key, String value, Long expireTime, TimeUnit timeUnit) {
        setString(stringRedisTemplate, key, value, expireTime, timeUnit);
    }

    public void setString(StringRedisTemplate stringRedisTemplate, String key, String value, Long expireTime, TimeUnit timeUnit) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    /**
     * 批量方法，设置字符串的键值
     *
     * @param map
     */
    public void setString(Map<String, String> map) {
        setString(stringRedisTemplate, map);
    }

    public void setString(StringRedisTemplate stringRedisTemplate, Map<String, String> map) {
        try {
            stringRedisTemplate.opsForValue().multiSet(map);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), StringUtils.join(map.keySet())), ex);
        }
    }

    /**
     * 获取字符串键对应的字符串值
     *
     * @param key
     * @return
     */
    public String getString(String key) {
        return getString(stringRedisTemplate, key);
    }

    public String getString(StringRedisTemplate stringRedisTemplate, String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return "";
    }

    /**
     * 获取字符串的键列表所对应的字符串的值列表
     *
     * @param keys
     * @return
     */
    public List<String> multiGetString(List<String> keys) {
        return multiGetString(stringRedisTemplate, keys);
    }

    public List<String> multiGetString(StringRedisTemplate stringRedisTemplate, List<String> keys) {
        Set<String> hashSet = new HashSet<>(keys);
        return multiGetString(stringRedisTemplate, hashSet);
    }

    /**
     * 内部封装的批量方法，防止键列表过大引起OOM
     *
     * @param keys
     * @return
     */
    private List<String> multiGetString(StringRedisTemplate stringRedisTemplate, Set<String> keys) {
        List<String> valueList = new ArrayList<>();
        List<String> keyList = new ArrayList<>();
        for (String k : keys) {
            keyList.add(k);
            if (keyList.size() == count) {
                doMultiGetString(stringRedisTemplate, keyList, valueList);
            }
        }
        if (!keyList.isEmpty()) {
            doMultiGetString(stringRedisTemplate, keyList, valueList);
        }
        return valueList;
    }

    /**
     * 内部封装的批量方法，用于初始化值列表
     *
     * @param keyList
     * @param valueList
     */
    private void doMultiGetString(StringRedisTemplate stringRedisTemplate, List<String> keyList, List<String> valueList) {
        try {
            List<String> list = stringRedisTemplate.opsForValue().multiGet(keyList);
            if (list != null && !list.isEmpty()) {
                valueList.addAll(list);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), StringUtils.join(keyList)), ex);
        } finally {
            keyList.clear();
        }
    }

    // zset add
    public <T> void zset_add(String key, T value, double score, Long expireTime, TimeUnit timeUnit) {
        zset_add(redisTemplate, key, value, score, expireTime, timeUnit);
    }

    public <T> void zset_add(RedisTemplate<String, Object> redisTemplate, String key, T value, double score, Long expireTime, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
            if (expireTime != null && timeUnit != null) {
                redisTemplate.expire(key, expireTime, timeUnit);
            }
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    // zset remove by score
    public <T> void zset_remove(String key, double score1, double score2) {
        zset_remove(redisTemplate, key, score1, score2);
    }

    public <T> void zset_remove(RedisTemplate<String, Object> redisTemplate, String key, double score1, double score2) {
        try {
            redisTemplate.opsForZSet().removeRangeByScore(key, score1, score2);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
    }

    // zset size
    public <T> Long zset_size(String key) {
        return zset_size(redisTemplate, key);
    }

    public <T> Long zset_size(RedisTemplate<String, Object> redisTemplate, String key) {
        try {
            return redisTemplate.opsForZSet().zCard(key);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return 0L;
    }

    public Boolean expire(String key, Long expireTime, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, expireTime, timeUnit);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
        }
        return false;
    }

    public void leftPush(String key, Object value) {
        leftPush(redisTemplate, key, value);
    }

    public void leftPush(RedisTemplate<String, Object> redisTemplate, String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    public void rightPush(String key, Object value) {
        rightPush(redisTemplate, key, value);
    }

    public void rightPush(RedisTemplate<String, Object> redisTemplate, String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public <T> T rightPop(String key, Class<T> clazz) {
        return rightPop(redisTemplate, key, clazz);
    }

    public <T> T rightPop(RedisTemplate<String, Object> redisTemplate, String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForList().rightPop(key);
            if (value == null) {
                return null;
            }
            return JsonUtil.toObject(value, clazz);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
            return null;
        }
    }

    public <T> T leftPop(String key, Class<T> clazz) {
        return leftPop(redisTemplate, key, clazz);
    }

    public <T> T leftPop(RedisTemplate<String, Object> redisTemplate, String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForList().leftPop(key);
            if (value == null) {
                return null;
            }
            return JsonUtil.toObject(value, clazz);
        } catch (Exception ex) {
            log.error(String.format("%s; key:%s", ex.getMessage(), key), ex);
            return null;
        }
    }

    public interface RedisScanHandler {
        void doHandle(RedisTemplate<String, Object> redisTemplate, Set<String> keys);
    }
}
