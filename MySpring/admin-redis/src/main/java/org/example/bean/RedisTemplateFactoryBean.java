package org.example.bean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

/**
 * @author zhy
 * @date 2024/9/23 14:46
 */
public class RedisTemplateFactoryBean {
    private RedisProperties redisProperties;

    public RedisTemplateFactoryBean(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * 初始化 redis 连接
     *
     * @return
     */
    public RedisConnectionFactory create(boolean callAfterPropertiesSet) {
        LettuceConnectionFactory connectionFactory = null;
        // 初始化 redis 的连接池配置
        GenericObjectPoolConfig poolConfig = getPoolConfig();
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(redisProperties.getTimeout())
                .poolConfig(poolConfig)
                .build();

        // 初始化 redis 的基础连接配置
        // 1. 先加载单机模式
        // 2. 再加载哨兵模式
        // 3. 最后加载集群模式
        // 4. 如果同时配置了多种模式，则后一种模式会覆盖前一种模式

        // 单机模式
        String host = redisProperties.getHost();
        if (!StringUtils.isEmpty(host)) {
            RedisStandaloneConfiguration configuration = getRedisStandaloneConfiguration();
            connectionFactory = new LettuceConnectionFactory(configuration, clientConfig);
        }
        // 哨兵模式
        RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
        if (sentinel != null && sentinel.getNodes() != null && !sentinel.getNodes().isEmpty()) {
            RedisSentinelConfiguration configuration = getRedisSentinelConfiguration();
            connectionFactory = new LettuceConnectionFactory(configuration, clientConfig);
        }
        // 集群模式
        RedisProperties.Cluster cluster = redisProperties.getCluster();
        if (cluster != null && cluster.getNodes() != null && !cluster.getNodes().isEmpty()) { // 集群
            RedisClusterConfiguration configuration = getRedisClusterConfiguration();
            connectionFactory = new LettuceConnectionFactory(configuration, clientConfig);
        }
        if (callAfterPropertiesSet) {
            // 必须调用 afterPropertiesSet() 方法，否则 client、connectionProvider、reactiveConnectionProvider 都为空
            connectionFactory.afterPropertiesSet();
        }
        return connectionFactory;
    }

    /**
     * 初始化 redis 的基础连接配置（单机）
     *
     * @return
     */
    private RedisStandaloneConfiguration getRedisStandaloneConfiguration() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());
        configuration.setPassword(redisProperties.getPassword());
        configuration.setDatabase(redisProperties.getDatabase());
        return configuration;
    }

    /**
     * 初始化 redis 的基础连接配置（集群）
     *
     * @return
     */
    private RedisClusterConfiguration getRedisClusterConfiguration() {
        RedisClusterConfiguration configuration = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
        configuration.setPassword(redisProperties.getPassword());
        return configuration;
    }

    /**
     * 初始化 redis 的基础连接配置（哨兵）
     *
     * @return
     */
    private RedisSentinelConfiguration getRedisSentinelConfiguration() {
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration(redisProperties.getSentinel().getMaster(), new HashSet<>(redisProperties.getSentinel().getNodes()));
        configuration.setPassword(redisProperties.getPassword());
        return configuration;
    }

    /**
     * 初始化 redis 的连接池配置
     *
     * @return
     */
    private GenericObjectPoolConfig getPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        return poolConfig;
    }

    /**
     * 设置 key、value 的序列化方式
     *
     * @param redisTemplate
     */
    public void setSerializer(RedisTemplate<String, Object> redisTemplate) {
        Jackson2JsonRedisSerializer<Object> fastJsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // 设置值value的序列化方式，防止value中出现类的声明
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);

        // 设置键key的序列化方式，防止key中出现类型的声明
        redisTemplate.setKeySerializer(new StringSerializer());
        redisTemplate.setHashKeySerializer(new StringSerializer());
    }

    class StringSerializer implements RedisSerializer<Object> {
        private final Charset charset;

        public StringSerializer() {
            this(StandardCharsets.UTF_8);
        }

        public StringSerializer(Charset charset) {
            this.charset = charset;
        }


        @Override
        public byte[] serialize(Object o) throws SerializationException {
            return o == null ? null : String.valueOf(o).getBytes(charset);
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            return bytes == null ? null : new String(bytes, charset);
        }
    }
}
