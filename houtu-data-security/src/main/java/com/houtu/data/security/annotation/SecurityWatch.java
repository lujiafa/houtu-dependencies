package com.houtu.data.security.annotation;

import java.lang.annotation.*;

/**
 * 安全拦截处理器
 * <p>
 *     适用场景：对敏感字段进行加密存储或传递（如：手机号、银行卡、身份证等等），通常应用于Dao或Mapper层
 * </p>
 */
@Documented
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityWatch {

   boolean encrypt() default true;

   boolean encryptRecovery() default true;

   boolean decrypt() default true;

   String processorBeanName();
}
