package org.example.util;

import lombok.Getter;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhy
 * @date 2024/7/30 15:04
 */
@Component
public class SpringUtil implements ApplicationContextAware {
    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取bean工厂，用来实现动态注入bean
     * 不能使用其他类加载器加载bean
     * 否则会出现异常:类未找到，类未定义
     *
     * @return
     */
    public static DefaultListableBeanFactory getBeanFactory() {
        return (DefaultListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
    }

    public static void setProxyClassLoader(ClassLoader classLoader) {
        List<BeanPostProcessor> postProcessors = getBeanFactory().getBeanPostProcessors();
        for (BeanPostProcessor postProcessor : postProcessors) {
            if (postProcessor instanceof AbstractAutoProxyCreator) {
                ((AbstractAutoProxyCreator) postProcessor).setProxyClassLoader(classLoader);
            }
        }
    }

    public static List<Map<String, Object>> getAllBean() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] beans = getApplicationContext()
                .getBeanDefinitionNames();
        for (String beanName : beans) {
            Class<?> beanType = getApplicationContext()
                    .getType(beanName);
            Map<String, Object> map = new HashMap<>();
            map.put("BeanName", beanName);
            map.put("beanType", beanType);
            map.put("package", beanType.getPackage());
            list.add(map);
        }
        return list;
    }

    public static void registerBean(String name, BeanDefinition beanDefinition) {
        getBeanFactory().registerBeanDefinition(name, beanDefinition);
    }

    public static String[] getDependentBeans(String name) {
        DefaultListableBeanFactory beanFactory = getBeanFactory();
        return beanFactory.getDependentBeans(name);
    }

    public static void removeBean(String name) {
        DefaultListableBeanFactory beanFactory = getBeanFactory();
        if (beanFactory.containsSingleton(name)) {
            beanFactory.destroySingleton(name);
        }
        if (beanFactory.containsBeanDefinition(name)) {
            beanFactory.removeBeanDefinition(name);
        }
    }

    public static boolean containsBean(String name) {
        return getApplicationContext().containsBean(name);
    }

    public static Object getBean(String name) {
        ApplicationContext applicationContext1 = getApplicationContext();
        if (applicationContext1.containsBean(name)) {
            return applicationContext1.getBean(name);
        } else {
            return null;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static void resolveController(String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RequestMappingHandlerMapping handlerMapping = (RequestMappingHandlerMapping)getApplicationContext().getBean("requestMappingHandlerMapping");
        // 注册Controller
        Method method = handlerMapping.getClass()
                .getSuperclass()
                .getSuperclass()
                .getDeclaredMethod("detectHandlerMethods", Object.class);
        method.setAccessible(true);
        method.invoke(handlerMapping, name);
    }
}
