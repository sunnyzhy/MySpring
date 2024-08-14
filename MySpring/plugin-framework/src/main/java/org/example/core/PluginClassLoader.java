package org.example.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

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

    protected boolean isSpringBeanClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        //是否是接口
        if (clazz.isInterface()) {
            return false;
        }
        //是否是抽象类
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        if (clazz.getAnnotation(Component.class) != null) {
            return true;
        }
        if (clazz.getAnnotation(Repository.class) != null) {
            return true;
        }
        if (clazz.getAnnotation(Service.class) != null) {
            return true;
        }
        return false;
    }

}
