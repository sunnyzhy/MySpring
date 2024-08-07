package org.example.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * @author zhy
 * @date 2024/7/19 18:13
 *
 * 使用缓存注解的注意事项（参考@Async注解的注意事项）：
 * 1. 本类的普通方法调用带缓存注解的方法，注解无效
 * 2. 本类的普通方法调用其他类里带缓存注解的方法，注解有效
 */
@Service
public class CommService {
    @CacheEvict(cacheNames = "aList", allEntries = true)
    public void doDel(){

    }
}
