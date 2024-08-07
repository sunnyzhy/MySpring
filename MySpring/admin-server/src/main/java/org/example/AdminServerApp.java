package org.example;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableAdminServer
public class AdminServerApp
{
    public static void main(String[] args) {
        SpringApplication.run(AdminServerApp.class, args);
    }
}
