package com.houtu.actuator.autoconfigure;

import io.lettuce.core.RedisClient;
import io.lettuce.core.metrics.MicrometerCommandLatencyRecorder;
import io.lettuce.core.metrics.MicrometerOptions;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.redis.LettuceMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Lettuce 监控自动配置类，参考：org.springframework.boot.actuate.autoconfigure.metrics.redis.LettuceMetricsAutoConfiguration
 * @author jon
 * @date 2022年12月17日
 */

@AutoConfiguration(
        before = {RedisAutoConfiguration.class, LettuceMetricsAutoConfiguration.class},
        after = {MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class}
)
@ConditionalOnClass({RedisClient.class, MicrometerCommandLatencyRecorder.class})
@ConditionalOnBean({MeterRegistry.class})
public class ActuatorLettuceMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MicrometerOptions micrometerOptions(Environment environment) {
        Boolean enable = environment.getProperty("management.metrics.enable.lettuce", Boolean.class, true);
        if (!enable) {
            MicrometerOptions.builder().disable().build();
        }
        Boolean lettuceHistogram = environment.getProperty("management.metrics.distribution.percentiles-histogram.lettuce", Boolean.class, false);
        return MicrometerOptions.builder().histogram(lettuceHistogram).build();
    }
}
