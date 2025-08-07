package com.houtu.web.handler;

import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

/**
 * 异常错误工具类
 * @author jonlu
 * @date 2017/9/26
 */
public class ThrowableUtils {

    /**
     * 通过Throwable获取指定类型的Throwable对象，包含嵌套
     * @param throwable 参数异常对象
     * @param clazz 错误码对象类型
     * @return 错误码对象
     */
    public static <T> T getThrowable(@Nonnull Throwable throwable, @Nonnull Class<T> clazz) {
        if (clazz.isInstance(throwable))
            return (T) throwable;
        Throwable nestedThrowable = throwable.getCause();
        Set<Throwable> exceptions = new HashSet<>();
        while (nestedThrowable != null && !exceptions.contains(nestedThrowable)) {
            if (clazz.isInstance(nestedThrowable))
                return (T) nestedThrowable;
            exceptions.add(nestedThrowable);
            nestedThrowable = nestedThrowable.getCause();
        }
        return null;
    }

}
