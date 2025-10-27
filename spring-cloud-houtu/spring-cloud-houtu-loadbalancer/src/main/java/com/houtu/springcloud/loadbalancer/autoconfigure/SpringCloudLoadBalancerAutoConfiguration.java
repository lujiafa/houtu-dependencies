package com.houtu.springcloud.loadbalancer.autoconfigure;


import com.houtu.springcloud.loadbalancer.prop.SpringCloudLoadBalancerProperties;
import com.houtu.springcloud.loadbalancer.support.SpringCloudLoadBalancerClientConfiguration;
import com.houtu.springcloud.loadbalancer.support.condition.EnabledHintCondition;
import com.houtu.springcloud.loadbalancer.support.hint.HintFeignInterceptor;
import com.houtu.springcloud.loadbalancer.support.hint.HintGatewayWebFilter;
import com.houtu.springcloud.loadbalancer.support.hint.HintRequestHandlerInterceptor;
import com.houtu.springcloud.loadbalancer.support.hint.HintWebFilter;
import feign.Feign;
import javax.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 参考：
 * org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration
 * com.alibaba.cloud.nacos.loadbalancer.LoadBalancerNacosAutoConfiguration
 *
 * @author: jonlu
 * @date: 2023/9/15
 */

@AutoConfiguration
@EnableConfigurationProperties(SpringCloudLoadBalancerProperties.class)
@AutoConfigureBefore({LoadBalancerAutoConfiguration.class, ReactorLoadBalancerClientAutoConfiguration.class, LoadBalancerBeanPostProcessorAutoConfiguration.class})
public class SpringCloudLoadBalancerAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(SpringCloudLoadBalancerAutoConfiguration.class);

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean({WebMvcConfigurationSupport.class})
    @Conditional(EnabledHintCondition.class)
    public static class SpringMVCConfiguration {

        @Bean
        public WebMvcConfigurer loadbalanceWebMvcConfigurer() {
            logger.info("Enable SpringMVC full-link hint function request intercept.");
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(new HintRequestHandlerInterceptor()).order(Ordered.LOWEST_PRECEDENCE);
                }
            };
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass({WebFluxConfigurer.class})
    @ConditionalOnMissingBean({WebFluxConfigurationSupport.class})
    @Conditional(EnabledHintCondition.class)
    public static class SpringWebFluxConfiguration {
        @Bean
        @ConditionalOnClass(RoutePredicateHandlerMapping.class)
        @ConditionalOnProperty(name = {"spring.cloud.gateway.enabled"}, matchIfMissing = true)
        public WebFilter hintGatewayWebFilter(SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties) {
            logger.info("Enable SpringCloudGateway hint request filter.");
            return new HintGatewayWebFilter(springCloudLoadBalancerProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "hintGatewayWebFilter", type = "com.houtu.springcloud.loadbalancer.support.hint.HintGatewayWebFilter")
        public WebFilter hintWebFilter() {
            logger.info("Enable SpringWebFlux full-link hint function request filter.");
            return new HintWebFilter();
        }
    }


    @Configuration
    @ConditionalOnClass(Feign.class)
    @Conditional(EnabledHintCondition.class)
    public static class HintFeignConfiguration {
        @Bean
        public HintFeignInterceptor hintFeignInterceptor() {
            logger.info("Enable full-link hint function Feign interceptor.");
            return new HintFeignInterceptor();
        }
    }

    /**
     * 参考：
     * org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration/org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration（@LoadBalancerClients空配置默认LoadBalancerClientConfiguration）
     * com.alibaba.cloud.nacos.loadbalancer.LoadBalancerNacosAutoConfiguration
     */
    @Configuration
    @LoadBalancerClients(defaultConfiguration = {SpringCloudLoadBalancerClientConfiguration.class})
    static class LoadBalancerConfiguration {
    }

}
