package io.github.lujiafa.houtu.actuator.autoconfigure;

import io.lettuce.core.RedisClient;
import io.lettuce.core.metrics.MicrometerCommandLatencyRecorder;
import io.lettuce.core.metrics.MicrometerOptions;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.redis.LettuceMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Lettuce 监控自动配置类，参考：org.springframework.boot.actuate.autoconfigure.metrics.redis.LettuceMetricsAutoConfiguration
 * @author jon
 * @date 2022年11月07日
 */

@AutoConfiguration(
        before = {RedisAutoConfiguration.class, LettuceMetricsAutoConfiguration.class},
        after = {MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class}
)
@ConditionalOnClass({RedisClient.class, MicrometerCommandLatencyRecorder.class})
@ConditionalOnBean({MeterRegistry.class})
public class ActuatorRedisMetricsAutoConfiguration {

    @Configuration
    @ConditionalOnClass({RedisClient.class, MicrometerCommandLatencyRecorder.class})
    public static class LettuceMetricsConfiguration {

        static final String REDIS_LETTUCE = "redis.lettuce";

        @Bean
        @ConditionalOnMissingBean
        public MicrometerOptions micrometerOptions(MetricsProperties metricsProperties) {
            Map<String, Boolean> enableMap = metricsProperties.getEnable();
            Boolean enable = enableMap.get(REDIS_LETTUCE);
            if (!Boolean.TRUE.equals(enable)) {
                return MicrometerOptions.builder().disable().build();
            }
            MetricsProperties.Distribution distribution = metricsProperties.getDistribution();
            Boolean lettuceEnabled = distribution.getPercentilesHistogram().get(REDIS_LETTUCE);
            double[] percentiles = distribution.getPercentiles().get(REDIS_LETTUCE);
            MicrometerOptions.Builder builder = MicrometerOptions.builder()
                    .histogram(Boolean.TRUE.equals(lettuceEnabled))
                    .targetPercentiles(percentiles == null ? new double[0] : percentiles);
            return builder.build();
        }

    }
}
