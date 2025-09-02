package com.houtu.data.security.annotation;

import com.houtu.data.security.handler.SecurityProcessor;

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

   /**
    * 是否启用参数加密
    * @return true 启用，false 不启用
    */
   boolean encrypt() default true;

   /**
    * 所有被注解@SecurityParam的待加密Map类型参数或Securityable中Map类型字段（包含嵌套），当配置此数组后，仅对和配置Key匹配的String加密（嵌套Value中Securityable子类也会自动匹配）
    * @return
    */
   String[] encryptMapKeys() default {};

   /**
    * 是否启用结果解密
    * @return true 启用，false 不启用
    */
   boolean decrypt() default true;

   /**
    * 可自定义实现处理器 com.houtu.data.security.handler.SecurityProcessor
    * @return 处理器Bean名称
    */
   String processorBeanName() default "";

   /**
    * 可自定义实现处理器 com.houtu.data.security.handler.SecurityProcessor
    * @return 处理器Class
    */
   Class<? extends SecurityProcessor> processorClass() default SecurityProcessor.class;
}
