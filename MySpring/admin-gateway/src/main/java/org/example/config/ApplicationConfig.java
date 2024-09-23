package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @date 2024/9/23 10:45
 */
@Component
@ConfigurationProperties(prefix = "sign")
@Data
public class ApplicationConfig {
    public Boolean enable;
    public Long expire;
    public String secret;
}
