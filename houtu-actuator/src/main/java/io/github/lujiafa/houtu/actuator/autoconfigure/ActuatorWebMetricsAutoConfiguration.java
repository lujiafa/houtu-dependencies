package io.github.lujiafa.houtu.actuator.autoconfigure;

import io.github.lujiafa.houtu.actuator.metrics.webmvc.ResponseBodyAdviceAndWebMvcTagsContributor;
import org.springframework.boot.micrometer.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.restclient.autoconfigure.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
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
    @ConditionalOnProperty(prefix = "management.metrics.distribution.percentiles", name = {"http.server.requests"})
    @ConditionalOnClass(ResponseBodyAdvice.class)
    public static class ActuatorSpringMVCConfiguration {

        /**
         * 收集处理业务状态分布指标
         *
         * @return ResponseBodyAdviceAndWebMvcTagsContributor
         */
        @Bean
        @ConditionalOnMissingBean
        public ResponseBodyAdviceAndWebMvcTagsContributor responseBodyAdviceAndWebMvcTagsContributor() {
            return new ResponseBodyAdviceAndWebMvcTagsContributor();
        }
    }

}
