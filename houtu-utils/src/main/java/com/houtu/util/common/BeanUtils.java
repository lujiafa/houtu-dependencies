package com.houtu.util.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @date 2019年7月24日
 * @author jonlu
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {

    private static Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    /**
     * @param source 源对象
     * @param target 目标对象
     * @description 将源对象属性复制到指定目标对象
     */
    public static void smartCopyProperties(Object source, Object target) {
        if (source == null) {
            logger.info("parameter source is null");
            return;
        }
        if (target == null) {
            logger.info("parameter target is null");
            return;
        }
        try {
            copyProperties(source, target);
        } catch (BeansException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param source 源对象
     * @param target 目标对象
     * @param nonNullProperties 复制过程是否对空字段跳过
     * @description 将源对象属性复制到指定目标对象
     */
    public static void smartCopyProperties(Object source, Object target, boolean nonNullProperties) {
        if (source == null) {
            logger.info("parameter source is null");
            return;
        }
        if (target == null) {
            logger.info("parameter target is null");
            return;
        }
        copyProperties(source, target, nonNullProperties);
    }

    /**
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreProperties 自定义忽略字段名
     * @description 将源对象属性复制到指定目标对象
     */
    public static void smartCopyProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null) {
            logger.info("parameter source is null");
            return;
        }
        if (target == null) {
            logger.info("parameter target is null");
            return;
        }
        try {
            copyProperties(source, target, ignoreProperties);
        } catch (BeansException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param source      源对象
     * @param targetClass 目标类型
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例
     */
    public static <S, T> T smartCopyProperties(S source, Class<T> targetClass) {
        if (source == null) {
            logger.info("parameter source is null");
            return null;
        }
        if (targetClass == null) {
            logger.info("parameter targetClass is null");
            return null;
        }
        return copyProperties(source, targetClass);
    }

    /**
     * @param source           源对象
     * @param targetClass      目标类型
     * @param ignoreProperties 自定义忽略属性名数组
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例，可指定忽略属性/字段
     */
    public static <S, T> T smartCopyProperties(S source, Class<T> targetClass, String... ignoreProperties) {
        if (source == null) {
            logger.info("parameter source is null");
            return null;
        }
        if (targetClass == null) {
            logger.info("parameter targetClass is null");
            return null;
        }
        return copyProperties(source, targetClass, ignoreProperties);
    }

    /**
     * @param source      源对象
     * @param targetClass 目标类型
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例，可指定忽略属性/字段
     */
    public static <S, T> List<T> smartCopyProperties(List<S> source, Class<T> targetClass) {
        if (source == null) {
            logger.info("parameter source is null");
            return null;
        }
        if (targetClass == null) {
            logger.info("parameter targetClass is null");
            return null;
        }
        return copyProperties(source, targetClass);
    }

    /**
     * @param source           源对象
     * @param targetClass      目标类型
     * @param ignoreProperties 自定义忽略属性名数组
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例，可指定忽略属性/字段
     */
    public static <S, T> List<T> smartCopyProperties(List<S> source, Class<T> targetClass, String... ignoreProperties) {
        if (source == null) {
            logger.info("parameter source is null");
            return null;
        }
        if (targetClass == null) {
            logger.info("parameter targetClass is null");
            return null;
        }
        return copyProperties(source, targetClass, ignoreProperties);
    }

    /**
     * @param source 源对象
     * @param target 目标对象
     * @param nonNullProperties 复制过程是否对空字段跳过
     * @description 将源对象属性复制到指定目标对象
     */
    public static void copyProperties(Object source, Object target, boolean nonNullProperties) {
        if (nonNullProperties && source != null) {
            Field[] fields = source.getClass().getDeclaredFields();
            String[] ignoreProperties = Arrays.stream(fields).parallel().filter(f -> {
                try {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    return f.get(source) == null;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }).map(f -> f.getName()).toArray(String[]::new);
            copyProperties(source, target, ignoreProperties);
        } else {
            copyProperties(source, target);

        }
    }

    /**
     * @param source      源对象
     * @param targetClass 目标类型
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例
     */
    public static <S, T> T copyProperties(S source, Class<T> targetClass) {
        return copyProperties(source, targetClass, new String[0]);
    }

    /**
     * @param source           源对象
     * @param targetClass      目标类型
     * @param ignoreProperties 自定义忽略属性名数组
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例，可指定忽略属性/字段
     */
    public static <S, T> T copyProperties(S source, Class<T> targetClass, String... ignoreProperties) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(targetClass, "Target Class must not be null");
        try {
            T target = instantiateClass(targetClass);
            copyProperties(source, target, ignoreProperties);
            return target;
        } catch (BeanInstantiationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param source      源对象
     * @param targetClass 目标类型
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例，可指定忽略属性/字段
     */
    public static <S, T> List<T> copyProperties(List<S> source, Class<T> targetClass) {
        return copyProperties(source, targetClass, new String[0]);
    }

    /**
     * @param source           源对象
     * @param targetClass      目标类型
     * @param ignoreProperties 自定义忽略属性名数组
     * @return T 目标对象
     * @description 将源对象属性复制到指定目标类型新实例，可指定忽略属性/字段
     */
    @SuppressWarnings("unchecked")
    public static <S, T> List<T> copyProperties(List<S> source, Class<T> targetClass, String... ignoreProperties) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(targetClass, "Target Class must not be null");
        try {
            List<T> resultList = instantiateClass(source.getClass());
            if (source.size() > 0) {
                for (S s : source) {
                    if (s != null) {
                        T target = copyProperties(s, targetClass, ignoreProperties);
                        resultList.add(target);
                    } else {
                        resultList.add(null);
                    }
                }
            }
            return resultList;
        } catch (BeanInstantiationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}