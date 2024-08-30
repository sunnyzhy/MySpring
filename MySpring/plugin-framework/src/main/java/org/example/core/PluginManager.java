package org.example.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhy
 * @date 2024/7/31 14:24
 * <p>
 * 插件管理器
 */
@Slf4j
public class PluginManager {
    private static String pluginPath;
    private static ConcurrentHashMap<String, Plugin> pluginMap = new ConcurrentHashMap<>();

    static {
        String currentPath = System.getProperty("user.dir");
        pluginPath = currentPath + File.separator + "plugins";
    }

    /**
     * 加载插件
     *
     * @param pluginName
     * @return
     */
    public static Plugin loadPlugin(String pluginName) {
        File pluginDirectory = new File(pluginPath + File.separator + pluginName);
        if (!pluginDirectory.exists()) {
            log.warn("插件目录libs不存在");
            return null;
        }
        File[] files = pluginDirectory.listFiles();
        if (files == null || files.length == 0) {
            log.warn("插件列表为空");
            return null;
        }
        File file = files[0];
        String md5;
        try {
            md5 = getFileMd5(file);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
        if (StringUtils.isEmpty(md5)) {
            log.error("加载插件 " + pluginName + " 失败");
            return null;
        }
        Plugin plugin;
        if (pluginMap.containsKey(pluginName)) {
            plugin = pluginMap.get(pluginName);
            // 如果jar没有更新就直接返回
            if (md5.equals(plugin.getMd5())) {
                return plugin;
            }
            pluginMap.remove(pluginName);
        } else {
            plugin = new Plugin();
            plugin.setMd5(md5);
        }
        boolean load = plugin.load(file);
        // 如果插件加载失败，就必须卸载插件
        if (!load) {
            plugin.unload();
            return null;
        }
        pluginMap.put(pluginName, plugin);
        return plugin;
    }

    /**
     * 卸载插件
     *
     * @param pluginName
     */
    public static void unloadPlugin(String pluginName) {
        if (!pluginMap.containsKey(pluginName)) {
            return;
        }
        Plugin plugin = pluginMap.get(pluginName);
        if (plugin == null) {
            return;
        }
        plugin.unload();
        pluginMap.remove(pluginName);
    }

    private static String getFileMd5(File file) throws Exception {
        FileInputStream inputStream = new FileInputStream(file);
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[102400];
        int length = -1;
        while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
            messageDigest.update(buffer, 0, length);
        }
        inputStream.close();
        BigInteger bigInt = new BigInteger(1, messageDigest.digest());
        String md5 = bigInt.toString(16);
        // md5从第一个非0的字节开始计算
        if (md5.length() < 32) {
            md5 = StringUtils.leftPad(md5, 32, "0");
        }
        return md5;
    }

}
