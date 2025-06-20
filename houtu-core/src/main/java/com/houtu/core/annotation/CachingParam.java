package com.houtu.core.annotation;

import java.lang.annotation.*;

/**
 * @Description: 辅助缓存参数注解
 * @author jonlu
 * @date 2017/10/23
 */
@Documented
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CachingParam {
}
