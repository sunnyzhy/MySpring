package org.example.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Modifier;
import java.net.*;
import java.util.jar.JarFile;

/**
 * @author zhy
 * @date 2024/7/29 15:45
 */
@Getter
@Slf4j
class PluginClassLoader extends URLClassLoader {
    @Setter
    private JarFile jarFile;

    public PluginClassLoader() {
        super(new URL[]{}, findParentClassLoader());
    }

    private static ClassLoader findParentClassLoader() {
        // 使用自定义的 ClassLoader，避免使用 SystemClassLoader 加载 Class
        ClassLoader parent = Plugin.class.getClassLoader();
        if (parent == null) {
            parent = PluginClassLoader.class.getClassLoader();
        }
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        return parent;

//        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void addURL(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof JarURLConnection jarURLConnection) {
//                jarURLConnection.setUseCaches(true);
                jarFile = jarURLConnection.getJarFile();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        super.addURL(url);
    }

    @Override
    protected Class<?> findClass(final String name)
            throws ClassNotFoundException {
        return super.findClass(name);
    }

    protected ANNOTATION_ENUM getAnnotation(Class<?> clazz) {
        if (clazz == null) {
            return ANNOTATION_ENUM.OTHER;
        }
        //是否是接口
        if (clazz.isInterface()) {
            return ANNOTATION_ENUM.OTHER;
        }
        //是否是抽象类
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return ANNOTATION_ENUM.OTHER;
        }
        if (clazz.getAnnotation(Component.class) != null) {
            return ANNOTATION_ENUM.COMPONENT;
        }
        if (clazz.getAnnotation(Repository.class) != null) {
            return ANNOTATION_ENUM.REPOSITORY;
        }
        if (clazz.getAnnotation(Service.class) != null) {
            return ANNOTATION_ENUM.SERVICE;
        }
        if (clazz.getAnnotation(Controller.class) != null) {
            return ANNOTATION_ENUM.CONTROLLER;
        }
        if (clazz.getAnnotation(RestController.class) != null) {
            return ANNOTATION_ENUM.REST_CONTROLLER;
        }
        if (clazz.getAnnotation(Configuration.class) != null) {
            return ANNOTATION_ENUM.CONFIGURATION;
        }
        if (clazz.getAnnotation(Bean.class) != null) {
            return ANNOTATION_ENUM.BEAN;
        }
        return ANNOTATION_ENUM.OTHER;
    }

}
