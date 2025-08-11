package com.houtu.core.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.TimeZone;

public class LocaleBeanPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractLocaleResolver localeResolver) {
            Locale locale = getLocale(localeResolver);
            if (locale != null) {
                LocaleContextHolder.setDefaultLocale(locale);
            }
        }
        if (bean instanceof AbstractLocaleContextResolver localeResolver) {
            TimeZone timeZone = localeResolver.getDefaultTimeZone();
            if (timeZone != null) {
                LocaleContextHolder.setDefaultTimeZone(timeZone);
            }
        }
        return bean;
    }

    Locale getLocale(AbstractLocaleResolver localeResolver) {
        try {
            Method method = AbstractLocaleResolver.class.getDeclaredMethod("getDefaultLocale", new Class[0]);
            method.setAccessible(true);
            return (Locale) method.invoke(localeResolver, new Object[0]);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
