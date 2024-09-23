package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.MessagePlugin;

import org.example.core.Plugin;
import org.example.core.PluginManager;
import org.example.model.MessageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zhy
 * @date 2024/7/22 16:13
 */
@Service
@Slf4j
public class PluginService {
    public void all(MessageData messageData) {
        List<String> pluginNameList = messageData.getPluginNameList();
        if (pluginNameList.isEmpty()) {
            return;
        }
        for (String pluginName : pluginNameList) {
            Plugin plugin = PluginManager.loadPlugin(pluginName);
            if (plugin == null) {
                continue;
            }
            MessagePlugin<MessageData> instance = plugin.instance(MessagePlugin.class);
            if (instance == null) {
                continue;
            }
            instance.send(messageData);
        }
    }

    public void send(String pluginName, MessageData messageData) {
        Plugin plugin = PluginManager.loadPlugin(pluginName);
        if (plugin == null) {
            return;
        }
        MessagePlugin<MessageData> instance = plugin.instance(MessagePlugin.class);
        if (instance == null) {
            return;
        }
        instance.send(messageData);
    }

    public void unload(String pluginName) {
        PluginManager.unloadPlugin(pluginName);
    }
//
//    public void send0(String pluginName, MessageData messageData) {
//        Plugin<MessagePlugin<MessageData>> plugin = pluginManager.loadPlugin(pluginName, MessagePlugin.class);
//        if(plugin==null){
//            return;
//        }
//        plugin.getInstance().send(messageData);
////        manager.unloadPlugin(pluginName);
//    }

    public void send1(String pluginName, MessageData messageData) {
        String currentPath = System.getProperty("user.dir");
        File file = new File(currentPath, "libs");
        File[] files = file.listFiles();
        URL url = null;
        try {
            url = new URL("jar:file:" + files[0].getAbsolutePath() + "!/");
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            return;
        }
//        ModuleClassLoader classLoader = new ModuleClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
//        classLoader.initBean();
//        List<String> registeredBean = classLoader.getRegisteredBean();
//        System.out.println(registeredBean);
//        Object bean = SpringContextUtil.getBean(registeredBean.get(0));
//        System.out.println(bean);
    }


    //    private final ApplicationContext applicationContext;
//
//    public PluginService(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }
//    @Autowired
//    private MessagePluginToRegistry pluginRegistry;

    {
//        try {
//            String currentPath = System.getProperty("user.dir");
//            File directory = new File(currentPath, "libs");
//            if (directory.exists()) {
//                for (File file : directory.listFiles()) {
//                    URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/");
//                    URLConnection urlConnection = url.openConnection();
//                    JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
//                    // 获取jar文件
//                    JarFile jarFile = jarURLConnection.getJarFile();
//                    Enumeration<JarEntry> entries = jarFile.entries();
//                    while (entries.hasMoreElements()) {
//                        JarEntry jarEntry = entries.nextElement();
//                        if (jarEntry.getName().endsWith(".class")) {
//                            String className = jarEntry.getName().replace("/", ".");
//                            className = className.substring(0, className.length() - 6);
//                            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
//                            Class<?> clazz = urlClassLoader.loadClass(className);
////                            Object o = clazz.getDeclaredConstructor().newInstance();
////                            ((MessagePlugin) o).send("cc");
////                        clazz.getMethod("send",String.class).invoke("aa");
//
//                            SpringUtil.registerBean("className", clazz);
//                            Object bean = SpringUtil.getBean("className");
//                            System.out.println(bean);
//                            break;
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    @Autowired(required = false)
    private MessagePlugin<String> pluginInterface;

    public void doSend(int type) {
//        Object bean11 = SpringUtil.getBean("AliyunDemo");
        try {
            String currentPath = System.getProperty("user.dir");
            File directory = new File(currentPath, "libs");
            if (directory.exists()) {
                for (File file : directory.listFiles()) {
                    URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/");
                    URLConnection urlConnection = url.openConnection();
                    JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
                    // 获取jar文件
                    JarFile jarFile = jarURLConnection.getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        if (jarEntry.getName().endsWith(".class")) {
                            String className = jarEntry.getName().replace("/", ".");
                            className = className.substring(0, className.length() - 6);
                            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
                            Class<?> clazz = urlClassLoader.loadClass(className);

//                            Object o = clazz.getDeclaredConstructor().newInstance();
//                            ((MessagePlugin) o).send("cc");
//                        clazz.getMethod("send",String.class).invoke("aa");

//                            SpringUtil.registerBean(className, clazz);
//                            Object bean = SpringUtil.getBean(className);
//                            System.out.println(bean);
//                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String className;

    public void doSend1(int type) {
        try {
//            MessagePlugin<String> plugin = SpringUtil.getBean(className);
//            plugin.send("url");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MessagePlugin getTargetPlugin(int type) {
        ServiceLoader<MessagePlugin> serviceLoader = ServiceLoader.load(MessagePlugin.class);
        Iterator<MessagePlugin> iterator = serviceLoader.iterator();
        List<MessagePlugin> messagePlugins = new ArrayList<>();
        while (iterator.hasNext()) {
            MessagePlugin messagePlugin = iterator.next();
            messagePlugins.add(messagePlugin);
        }
        MessagePlugin targetPlugin = null;
//        for (MessagePlugin messagePlugin : messagePlugins) {
//            boolean findTarget = false;
//            switch (type) {
//                case "aliyun":
//                    if (messagePlugin instanceof BitptImpl){
//                        targetPlugin = messagePlugin;
//                        findTarget = true;
//                        break;
//                    }
//                case "tencent":
//                    if (messagePlugin instanceof MizptImpl){
//                        targetPlugin = messagePlugin;
//                        findTarget = true;
//                        break;
//                    }
//            }
//            if(findTarget) break;
//        }
        return targetPlugin;
    }

}
