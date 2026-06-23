package io.github.lujiafa.houtu.websecurity.autoconfigure;

import io.github.lujiafa.houtu.util.constant.CharConstant;
import io.github.lujiafa.houtu.websecurity.prop.RepeatProperties;
import io.github.lujiafa.houtu.websecurity.repeat.RepeatRequestValidator;
import io.github.lujiafa.houtu.websecurity.repeat.validator.RedisRepeatRequestValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(RepeatProperties.class)
public class RepeatRequestConfiguration {

    /**
     * 默认基于 Redis 的防重放验证器，仅在存在 redisTemplate 时实例化；
     * 当用户提供自定义 {@link RepeatRequestValidator} 实现时忽略默认。
     */
    @Bean
    @ConditionalOnBean(name = "redisTemplate")
    @ConditionalOnMissingBean(RepeatRequestValidator.class)
    public RepeatRequestValidator repeatRequestValidator(Environment environment,
                                                         RepeatProperties repeatProperties,
                                                         @Qualifier("redisTemplate") RedisTemplate<String, ?> redisTemplate) {
        String applicationName = environment.getProperty("spring.application.name", CharConstant.HYPHEN);
        return new RedisRepeatRequestValidator((RedisTemplate<String, Object>) redisTemplate, applicationName, repeatProperties);
    }
}
