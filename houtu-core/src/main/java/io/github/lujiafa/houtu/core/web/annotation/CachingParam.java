package io.github.lujiafa.houtu.core.web.annotation;

import java.lang.annotation.*;

/**
 * @Description: 注解支持检测需要缓存参数模块
 * @author jonlu
 * @date 2017/10/23
 */
@Documented
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CachingParam {
}
