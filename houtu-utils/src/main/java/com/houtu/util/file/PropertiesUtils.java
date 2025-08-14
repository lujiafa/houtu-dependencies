package com.houtu.util.file;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

/**
 * @author jonlu
 * @Description:properties文件操作工具类
 */
public class PropertiesUtils {

    /**
     * @param targetFileNamem properties文件地址
     * @param map             需要写入properties文件的键值对
     * @Description:向properties文件写入键值对
     */
    public static void write(String targetFileNamem, Map<String, String> map) {
        write(targetFileNamem, map, null);
    }

    /**
     * @param targetFileName properties文件地址
     * @param map            需要写入properties文件的键值对
     * @param comment        属性列表的描述
     * @return boolean 是否写入成功
     * @Description:向properties文件写入键值对
     */
    public static void write(String targetFileName, Map<String, String> map, String comment) {
        Assert.isTrue(!StringUtils.isEmpty(targetFileName), "文件地址参数不能为空");
        File file = new File(targetFileName);
        try {
            if (!file.exists())
                file.createNewFile();
            try (OutputStream os = new FileOutputStream(file)) {
                Properties prop = new Properties();
                if (map != null) {
                    Set<String> keys = map.keySet();
                    for (String key : keys) {
                        prop.setProperty(key, map.get(key));
                    }
                    prop.store(os, comment);
                }
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param sourceFileName properties文件地址
     * @param key            键
     * @return String 键对应值
     * @Description:通过key和路径获取properties文件键值
     */
    public static String read(String sourceFileName, String key) {
        Map<String, String> map = read(sourceFileName);
        return map.get(key);
    }

    /**
     * @param sourceFileName properties文件地址
     * @return map 存储properties文件中键值对
     * @Description:通过properties文件路径获取相应Map键值
     */
    public static Map<String, String> read(String sourceFileName) {
        Assert.isTrue(!StringUtils.isEmpty(sourceFileName), "文件地址参数不能为空");
        File file = new File(sourceFileName);
        Assert.isTrue(file.exists(), "文件不存在，" + sourceFileName);
        Map<String, String> map = new HashMap<String, String>();
        Properties prop = new Properties();
        try (InputStream is = new FileInputStream(file)) {
            prop.load(is);
            Enumeration<?> enums = prop.propertyNames();
            while (enums.hasMoreElements()) {
                String key = String.valueOf(enums.nextElement());
                map.put(key, prop.getProperty(key));
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return map;
    }

}