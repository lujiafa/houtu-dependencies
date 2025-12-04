package com.houtu.actuator.autoconfigure;

import com.houtu.actuator.metrics.client.ActuatorHttpClient5ExecChainHandler;
import com.houtu.actuator.metrics.client.DefaultHttpClient5ExecChainTagsProvider;
import com.houtu.actuator.metrics.client.EnabledClientMetricsCondition;
import com.houtu.actuator.metrics.client.HttpClient5ExecChainTagsProvider;
import com.houtu.actuator.metrics.webmvc.EnabledServerMetricsCondition;
import com.houtu.actuator.metrics.webmvc.ResponseBodyAdviceAndWebMvcTagsContributor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author jon
 * @date 2020年12月17日
 */
@AutoConfiguration(after = {MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class, RestTemplateAutoConfiguration.class})
public class ActuatorWebMetricsAutoConfiguration {

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Conditional(EnabledServerMetricsCondition.class)
    @ConditionalOnClass(ResponseBodyAdvice.class)
    public static class ActuatorSpringMVCConfiguration {

        /**
         * 收集处理业务状态分布指标
         * @return ResponseBodyAdviceAndWebMvcTagsContributor
         */
        @Bean
        @ConditionalOnMissingBean
        public ResponseBodyAdviceAndWebMvcTagsContributor responseBodyAdviceAndWebMvcTagsContributor() {
            return new ResponseBodyAdviceAndWebMvcTagsContributor();
        }
    }

    @Configuration
    @Conditional(EnabledClientMetricsCondition.class)
    public static class ActuatorHttpClient5Configuration {

        @Bean
        @ConditionalOnMissingBean
        public HttpClient5ExecChainTagsProvider httpClient5ExecChainTagsProvider() {
            return new DefaultHttpClient5ExecChainTagsProvider();
        }

        @Bean
        @ConditionalOnMissingBean
        public ActuatorHttpClient5ExecChainHandler actuatorHttpClient5ExecChainHandler(MeterRegistry meterRegistry, MetricsProperties properties, HttpClient5ExecChainTagsProvider httpClient5ExecChainTagsProvider) {
            MetricsProperties.Web.Client.ClientRequest request = properties.getWeb().getClient().getRequest();
            return new ActuatorHttpClient5ExecChainHandler(meterRegistry, request.getMetricName(), request.getAutotime(), httpClient5ExecChainTagsProvider);
        }

    }
}
