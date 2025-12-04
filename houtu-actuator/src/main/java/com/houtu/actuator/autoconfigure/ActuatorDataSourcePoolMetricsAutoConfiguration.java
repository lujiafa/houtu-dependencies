package com.houtu.actuator.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jon
 * @date 2022年12月17日
 */

@AutoConfiguration(before = {DataSourcePoolMetricsAutoConfiguration.class})
@ConditionalOnClass({HikariDataSource.class, MeterRegistry.class})
@ConditionalOnBean({DataSource.class, MeterRegistry.class})
public class ActuatorDataSourcePoolMetricsAutoConfiguration implements BeanDefinitionRegistryPostProcessor {

    private Map<DataSource, String> dataSourceExistMap = new HashMap<>();
    private Map<String, Map<String, DataSource>> dataSourceMaps = new HashMap<>();

    public ActuatorDataSourcePoolMetricsAutoConfiguration(ApplicationContext applicationContext, ObjectProvider<Map<String, DataSource>> dataSourceMapProvider) {
        Map<String, DataSource> dataSourceMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, DataSource.class, false, true);
        if (dataSourceMap != null && !dataSourceMap.isEmpty()) {
            dataSourceMap.entrySet().stream().forEach(entry -> {
                dataSourceExistMap.put(entry.getValue(), entry.getKey());
            });
        }
        Map<String, Map<String, DataSource>> tmpDataSourceMap = (Map<String, Map<String, DataSource>>) BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ResolvableType.forClassWithGenerics(Map.class, String.class, DataSource.class).resolve());
        if (tmpDataSourceMap != null && !tmpDataSourceMap.isEmpty()) {
            dataSourceMaps.putAll(tmpDataSourceMap);
        }
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        dataSourceMaps.entrySet().stream().forEach(mapEntry -> {
            mapEntry.getValue().entrySet().stream().forEach(entry -> {
                DataSource dataSource = entry.getValue();
                if (dataSource instanceof HikariDataSource && !dataSourceExistMap.containsKey(dataSource)) {
                    String dataSourceName = mapEntry.getKey() + "." + entry.getKey();
                    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                    beanDefinition.setBeanClass(HikariDataSource.class);
                    beanDefinition.setInstanceSupplier(() -> dataSource);
                    registry.registerBeanDefinition(dataSourceName, beanDefinition);
                }
            });
        });
    }
}
