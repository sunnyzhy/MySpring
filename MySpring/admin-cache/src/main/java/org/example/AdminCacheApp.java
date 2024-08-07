package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableCaching
@MapperScan(basePackages = "org.example.mapper")
public class AdminCacheApp
{
    public static void main(String[] args) {
        SpringApplication.run(AdminCacheApp.class, args);
    }
}
