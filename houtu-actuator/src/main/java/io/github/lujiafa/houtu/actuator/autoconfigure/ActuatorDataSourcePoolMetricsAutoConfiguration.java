package io.github.lujiafa.houtu.actuator.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jon
 * @date 2022年12月17日
 */

@AutoConfiguration(after = {DataSourcePoolMetricsAutoConfiguration.class, MetricsAutoConfiguration.class, DataSourceAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnClass({HikariDataSource.class, MeterRegistry.class})
@ConditionalOnBean({MeterRegistry.class})
public class ActuatorDataSourcePoolMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSourcePoolMetricsBeanPostProcessor dataSourcePoolMetricsBeanPostProcessor(MeterRegistry meterRegistry) {
        return new DataSourcePoolMetricsBeanPostProcessor(meterRegistry);
    }

    public static class DataSourcePoolMetricsBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton {

        protected Map<HikariDataSource, String> dataSourceMap = new HashMap<>();
        protected MeterRegistry meterRegistry;

        public DataSourcePoolMetricsBeanPostProcessor(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof HikariDataSource && !dataSourceMap.containsKey(bean)) {
                dataSourceMap.put((HikariDataSource) bean, beanName);
                if (((HikariDataSource) bean).getPoolName() == null) {
                    ((HikariDataSource) bean).setPoolName(beanName);
                }
            } else if (bean instanceof Map && !((Map<?, ?>) bean).isEmpty()) {
                ((Map<?, ?>) bean).entrySet().stream().forEach(entry -> {
                    Object value = entry.getValue();
                    if (value instanceof HikariDataSource && !dataSourceMap.containsKey(value)) {
                        HikariDataSource hikariDataSource = (HikariDataSource) value;
                        String dataSourceName = beanName + "." + entry.getKey();
                        if (hikariDataSource.getPoolName() == null) {
                            hikariDataSource.setPoolName(dataSourceName);
                        }
                        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                        beanDefinition.setBeanClass(HikariDataSource.class);
                        beanDefinition.setInstanceSupplier(() -> hikariDataSource);
                        dataSourceMap.put(hikariDataSource, dataSourceName);
                    }
                });
            }
            return bean;
        }

        @Override
        public void afterSingletonsInstantiated() {
            if (!dataSourceMap.isEmpty()) {
                dataSourceMap.entrySet().stream().forEach(entry -> {
                    HikariDataSource dataSource = entry.getKey();
                    if (dataSource.getMetricRegistry() == null) {
                        if (dataSource.getMetricsTrackerFactory() == null) {
                            dataSource.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory(meterRegistry));
                        }
                    }
                });
            }
            dataSourceMap.clear();
        }
    }
}
