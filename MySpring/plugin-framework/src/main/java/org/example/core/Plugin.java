package org.example.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.util.SpringUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zhy
 * @date 2024/7/29 15:46
 * <p>
 * 插件
 */
@Slf4j
public class Plugin {
    @Setter
    @Getter
    private String md5;

    @Getter
    private ConcurrentHashMap<String, PluginInfo> cacheClass = new ConcurrentHashMap<>();

    private PluginClassLoader classLoader;

    /**
     * 加载插件
     *
     * @param file
     * @return
     */
    public boolean load(File file) {
        // jar文件有更新
        PluginClassLoader classLoader = new PluginClassLoader();
        try {
            URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/");
            classLoader.addURL(url);
            this.classLoader = classLoader;
            // 获取jar文件
            JarFile jarFile = classLoader.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.getName().endsWith(".class")) {
                    continue;
                }
                String className = jarEntry.getName().replace("/", ".");
                className = className.substring(0, className.length() - 6);
                // 此处无需双亲委派，所以使用用findClass而不是loadClass
                Class<?> clazz = classLoader.findClass(className);
                PluginInfo pluginInfo = new PluginInfo();
                pluginInfo.clazz = clazz;
                if (classLoader.isSpringBeanClass(clazz)) {
                    if (SpringUtil.containsBean(className)) {
                        SpringUtil.removeBean(className);
                    }
                    pluginInfo.isSpringBeanClass = true;
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                    BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
                    beanDefinition.setScope("singleton");
                    SpringUtil.registerBean(className, beanDefinition);
                    log.info("注册bean:" + className);
                }
                cacheClass.put(className, pluginInfo);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 实例化对象或从spring容器里取现对象
     *
     * @param pluginInterface
     * @return
     */
    public <T> T instance(Class<?> pluginInterface) {
        Object o = null;
        for (Map.Entry<String, PluginInfo> entry : cacheClass.entrySet()) {
            PluginInfo pluginInfo = entry.getValue();
            Class<?> clazz = pluginInfo.clazz;
            if (pluginInterface.isAssignableFrom(clazz)) {
                if (pluginInfo.instance == null) {
                    if (pluginInfo.isSpringBeanClass) {
                        String className = entry.getKey();
                        pluginInfo.instance = SpringUtil.getBean(className);
                    } else {
                        try {
                            pluginInfo.instance = clazz.getDeclaredConstructor().newInstance();
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
                o = pluginInfo.instance;
                break;
            }
        }
        return o == null ? null : (T) o;
    }

    /**
     * 卸载插件
     */
    public void unload() {
        // 释放插件里缓存的对象
        clearCacheClass();
        if (classLoader == null) {
            return;
        }
        try {
            // 卸载classLoader
            // 这一步很重要，否则会出现 src.refs 的计数大于 1 的情况，如果出现的话，插件是热卸载不掉的）
            classLoader.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        JarFile jarFile = classLoader.getJarFile();
        if (jarFile == null) {
            return;
        }
        try {
            // 卸载jarFile
            jarFile.close();
            log.info("卸载插件完成：" + jarFile.getName());
        } catch (IOException e) {
            log.error("卸载插件失败：" + jarFile.getName());
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 释放插件里缓存的对象
     */
    private void clearCacheClass() {
        if (cacheClass.isEmpty()) {
            return;
        }
        Map<String, String[]> dependentBeanMap = new HashMap<>();
        for (Map.Entry<String, PluginInfo> entry : cacheClass.entrySet()) {
            if (!entry.getValue().isSpringBeanClass) {
                continue;
            }
            String key = entry.getKey();
            String[] dependentBeans = SpringUtil.getDependentBeans(key);
            dependentBeanMap.put(key, dependentBeans);
        }
        // 移除bean
        while (!dependentBeanMap.isEmpty()) {
            String beanName = null;
            for (Map.Entry<String, String[]> entry : dependentBeanMap.entrySet()) {
                beanName = findDeleteBeanName(entry.getKey(), dependentBeanMap);
                if (beanName != null) {
                    break;
                }
            }
            if (beanName == null) {
                continue;
            }
            // 移除dependentBeanMap里各项所依赖的beanName
            Iterator<Map.Entry<String, String[]>> it = dependentBeanMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String[]> next = it.next();
                String[] value = next.getValue();
                if (value.length == 0) {
                    continue;
                }
                String[] arr = new String[value.length - 1];
                int delIndex = -1;
                for (int i = 0; i < value.length; i++) {
                    if (value[i].equals(beanName)) {
                        delIndex = i;
                        break;
                    }
                }
                // 没有找到匹配的元素
                if (delIndex == -1) {
                    continue;
                }
                for (int i = 0; i < delIndex; i++) {
                    arr[i] = value[i];
                }
                for (int i = delIndex; i < value.length - 1; i++) {
                    arr[i] = value[i + 1];
                }
                next.setValue(arr);
            }
            // 从Spring容器里移除bean
            SpringUtil.removeBean(beanName);
            // 从dependentBeanMap里删除没有被依赖的beanName
            dependentBeanMap.remove(beanName);
            // 从cacheClass里删除beanName
            PluginInfo pluginInfo = cacheClass.get(beanName);
            pluginInfo.clazz = null;
            pluginInfo.instance = null;
            cacheClass.remove(beanName);
        }
        // 移除普通类
        if (cacheClass.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, PluginInfo>> iterator = cacheClass.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PluginInfo> next = iterator.next();
            PluginInfo pluginInfo = next.getValue();
            pluginInfo.clazz = null;
            pluginInfo.instance = null;
            iterator.remove();
        }
    }

    private String findDeleteBeanName(String name, Map<String, String[]> dependentBeanMap) {
        String[] dependentBeanNames = dependentBeanMap.get(name);
        if (dependentBeanNames.length == 0) {
            return name;
        }
        for (String dependentBeanName : dependentBeanNames) {
            return findDeleteBeanName(dependentBeanName, dependentBeanMap);
        }
        return null;
    }

    class PluginInfo {
        private Class<?> clazz;
        private boolean isSpringBeanClass;
        private Object instance;
    }

}
