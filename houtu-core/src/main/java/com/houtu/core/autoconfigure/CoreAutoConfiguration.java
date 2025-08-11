package com.houtu.core.autoconfigure;

import com.houtu.core.context.LocaleBeanPostProcessor;
import com.houtu.core.context.SpringApplicationContext;
import com.houtu.core.exception.ErrorI18nProvider;
import com.houtu.core.exception.ErrorResourceBundleMessageSource;
import com.houtu.core.exception.provier.DefaultErrorI18nProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
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
    public ErrorI18nProvider coreErrorI18nProvider() {
        return new DefaultErrorI18nProvider();
    }

    @Bean
    public MessageSource errorMessageSource(List<ErrorI18nProvider> errorI18nProviders) {
        List<ErrorI18nProvider> providers = errorI18nProviders.stream().filter(p -> StringUtils.hasLength(p.getBasename())).collect(Collectors.toList());
        AnnotationAwareOrderComparator.sort(providers);
        ErrorResourceBundleMessageSource messageSource = new ErrorResourceBundleMessageSource();
        // 指定basename
        messageSource.setBasenames(providers.stream().map(ErrorI18nProvider::getBasename).toArray(String[]::new));
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(-1);
        return messageSource;
    }

    @Bean
    public LocaleBeanPostProcessor localeBeanPostProcessor() {
        return new LocaleBeanPostProcessor();
    }
}
