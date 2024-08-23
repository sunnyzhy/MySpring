package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class PluginDemoApp
{
    public static void main( String[] args )
    {
        SpringApplication.run(PluginDemoApp.class, args);
        System.out.println("main");
    }
}
