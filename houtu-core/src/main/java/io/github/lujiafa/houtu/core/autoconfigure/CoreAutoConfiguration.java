package io.github.lujiafa.houtu.core.autoconfigure;

import io.github.lujiafa.houtu.core.context.SpringApplicationContext;
import io.github.lujiafa.houtu.core.context.webflux.LocaleContextBeanPostProcessor;
import io.github.lujiafa.houtu.core.context.webmvc.LocaleBeanPostProcessor;
import io.github.lujiafa.houtu.core.exception.ErrorI18nProvider;
import io.github.lujiafa.houtu.core.exception.ErrorResourceBundleMessageSource;
import io.github.lujiafa.houtu.core.exception.provier.DefaultErrorI18nProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1)
public class CoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringApplicationContext springApplicationContext() {
        return new SpringApplicationContext();
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultErrorI18nProvider")
    public ErrorI18nProvider defaultErrorI18nProvider() {
        return new DefaultErrorI18nProvider();
    }

    @Bean
    @ConditionalOnMissingBean(name = "errorMessageSource")
    public MessageSource errorMessageSource(List<ErrorI18nProvider> errorI18nProviders) {
        /**
         * 设置错误信息源
         * 参考：org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration
         */
        List<ErrorI18nProvider> providers = errorI18nProviders.stream().filter(p -> StringUtils.hasLength(p.getBasename())).collect(Collectors.toList());
        AnnotationAwareOrderComparator.sort(providers);
        ErrorResourceBundleMessageSource messageSource = new ErrorResourceBundleMessageSource();
        messageSource.setBasenames(providers.stream().map(ErrorI18nProvider::getBasename).toArray(String[]::new));
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(-1);
        return messageSource;
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class SpringMVCConfiguration {
        @Bean
        public LocaleBeanPostProcessor localeContextBeanPostProcessor() {
            return new LocaleBeanPostProcessor();
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class SpringWebFluxConfiguration {
        @Bean
        public LocaleContextBeanPostProcessor localeContextBeanPostProcessor() {
            return new LocaleContextBeanPostProcessor();
        }
    }
}
