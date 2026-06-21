package io.github.lujiafa.houtu.accesslog.autoconfigure;

import io.github.lujiafa.houtu.accesslog.aspect.AccessLogAspect;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Import(AccessLogCombineModelMapConfiguration.class)
public class AccessLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AccessLogAspect accessLogAspect() {
        return new AccessLogAspect();
    }

}
