package io.github.lujiafa.houtu.springcloud.feign.anotation;

import java.lang.annotation.*;

/**
 * @description 注解自动发布
 * @date 2019年8月14日
 * @author jonlu
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFeign {

    /**
     * 是否启用自动Feign发布
     * @return true-启用 false-关闭
     */
    boolean value() default true;

}