package io.github.lujiafa.houtu.core.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;

import java.util.Collections;
import java.util.Map;

@Scope(value = "singleton")
public class SpringApplicationContext implements ApplicationContextAware {

    static Logger logger = LoggerFactory.getLogger(SpringApplicationContext.class);

    static ApplicationContext context;

    public static ApplicationContext getApplicationContext() {
        return context;
    }


    /**
     * @Title getBean
     * @Description 通过Bean名称获取实例
     * @param name Bean实例注册名称
     * @throws BeansException
     */
    public static Object getBean(String name) {
        try {
            return context.getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    /**
     * @Title getBean
     * @Description 通过Bean类型获取Bean实例
     * @param requiredType Bean类型
     * @return T
     */
    public static <T> T getBean(Class<T> requiredType) {
        try {
            return BeanFactoryUtils.beanOfType(context, requiredType);
        } catch (NoSuchBeanDefinitionException e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    /**
     * @Title getBean
     * @Description 通过Bean名称和类型获取匹配的Bean实例
     * @param name         Bean名称
     * @param requiredType Bean类型
     * @return T
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        try {
            return context.getBean(name, requiredType);
        } catch (NoSuchBeanDefinitionException e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    /**
     * @Title getBeansOfType
     * @Description 通过类型获取对应名称/实例Map
     * @param type 类型
     * @return Map<String, T>
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        Map<String, T> beansMap = null;
        try {
            beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, type);
        } catch (NoSuchBeanDefinitionException e) {
            logger.warn(e.getMessage());
        }
        if (beansMap == null) {
            beansMap = Collections.emptyMap();
        }
        return beansMap;
    }

    /**
     * @Title containsBean
     * @Description 判断Bean工厂实例中是否包含该Bean名称
     * @param name Bean名称
     * @return boolean
     */
    public static boolean containsBean(String name) {
        return context.containsBean(name);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
