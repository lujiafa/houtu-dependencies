package io.github.lujiafa.houtu.id.autoconfigure;

import io.github.lujiafa.houtu.id.prop.IdProperties;
import io.github.lujiafa.houtu.id.workid.WorkerIdProvider;
import io.github.lujiafa.houtu.id.workid.db.DbWorkerIdOptions;
import io.github.lujiafa.houtu.id.workid.db.DbWorkerIdProvider;
import io.github.lujiafa.houtu.id.workid.redis.RedisWorkerIdOptions;
import io.github.lujiafa.houtu.id.workid.redis.RedisWorkerIdProvider;
import io.github.lujiafa.houtu.util.common.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration ;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月19日
 * @Description houtu-id 模块的 Spring Boot AutoConfiguration。
 *
 * <p>根据 {@code houtu.id.work-id.type} 显式装配 {@link WorkerIdProvider}：
 * <ul>
 *   <li>{@code type=redis} + classpath 有 {@link StringRedisTemplate} + 容器有该 bean → {@link RedisWorkerIdProvider}</li>
 *   <li>{@code type=db}    + classpath 有 {@link JdbcTemplate}        + 容器有该 bean → {@link DbWorkerIdProvider}</li>
 *   <li>未配置 {@code type} → 不创建任何 WorkerIdProvider bean</li>
 * </ul>
 *
 * <p>identity 端口动态取自 {@code server.port}；ip 由 Provider 自动解析本机网卡（loopback 时降级 UUID）。
 * 用户自定义同类型 bean 时通过 {@link ConditionalOnMissingBean} 让位。
 */
@AutoConfiguration(after = { DataRedisAutoConfiguration.class, JdbcTemplateAutoConfiguration.class })
@EnableConfigurationProperties(IdProperties.class)
public class IdAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IdAutoConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnProperty(prefix = "houtu.id.work-id", name = "type", havingValue = "redis", matchIfMissing = true)
    static class RedisWorkerIdConfiguration {

        @Bean
        @ConditionalOnMissingBean(WorkerIdProvider.class)
        public WorkerIdProvider workerIdProvider(StringRedisTemplate stringRedisTemplate,
                                                 IdProperties properties,
                                                 Environment environment) {
            IdProperties.WorkId w = properties.getWorkId();
            RedisWorkerIdOptions.Builder builder = RedisWorkerIdOptions.builder()
                    .workerBits(w.getWorkerBits());
            String ip = SystemUtils.getServerIp();
            if (StringUtils.isNoneEmpty(ip)) {
                builder.ip(ip);
            }
            String serverPort = environment.getProperty("server.port");
            if (StringUtils.isNumeric(serverPort)) {
                builder.port(Integer.parseInt(serverPort));
            }
            return new RedisWorkerIdProvider(stringRedisTemplate, builder.build());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnProperty(prefix = "houtu.id.work-id", name = "type", havingValue = "db")
    static class DbWorkerIdConfiguration {

        @Bean
        @ConditionalOnMissingBean(WorkerIdProvider.class)
        public WorkerIdProvider workerIdProvider(JdbcTemplate jdbcTemplate,
                                                 IdProperties properties,
                                                 Environment environment) {
            IdProperties.WorkId w = properties.getWorkId();
            DbWorkerIdOptions.Builder builder = DbWorkerIdOptions.builder()
                    .workerBits(w.getWorkerBits());
            String ip = SystemUtils.getServerIp();
            if (StringUtils.isNoneEmpty(ip)) {
                builder.ip(ip);
            }
            String serverPort = environment.getProperty("server.port");
            if (StringUtils.isNumeric(serverPort)) {
                builder.port(Integer.parseInt(serverPort));
            }
            return new DbWorkerIdProvider(jdbcTemplate, builder.build());
        }
    }
}
