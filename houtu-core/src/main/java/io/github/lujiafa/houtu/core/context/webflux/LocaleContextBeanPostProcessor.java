package io.github.lujiafa.houtu.core.context.webflux;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;

import java.util.Locale;

public class LocaleContextBeanPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AcceptHeaderLocaleContextResolver) {
            Locale locale = ((AcceptHeaderLocaleContextResolver) bean).getDefaultLocale();
            if (locale != null) {
                LocaleContextHolder.setDefaultLocale(locale);
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
