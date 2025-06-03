package com.houtu.data.security.aspect;

import com.houtu.data.security.annotation.SecurityField;
import com.houtu.data.security.annotation.SecurityMapKey;
import com.houtu.data.security.annotation.SecurityParam;
import com.houtu.data.security.annotation.SecurityWatch;
import com.houtu.data.security.handler.SecurityProcessor;
import com.houtu.data.security.support.Securityable;
import com.houtu.data.security.support.SetterContextInfo;
import com.houtu.util.common.AnnotationUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author jonlu
 * @date 2019年5月29日
 */
@Aspect
public class SecurityWatchAspect implements Ordered {

    private ApplicationContext applicationContext;
    private SecurityProcessor defaultSecurityProcessor;

    public SecurityWatchAspect(ApplicationContext applicationContext, SecurityProcessor defaultSecurityProcessor) {
        this.applicationContext = applicationContext;
        this.defaultSecurityProcessor = defaultSecurityProcessor;
    }

    @Pointcut("@within(com.houtu.data.security.annotation.SecurityWatch) || @annotation(com.houtu.data.security.annotation.SecurityWatch)")
    public void daoWatchPointcut() {
    }

    //    @Around("daoWatchPointcut() || execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.*(..))")
    @Around("daoWatchPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        SecurityWatch securityWatch = AnnotationUtils.getAnnotationByPriorityMethod(method, SecurityWatch.class);
        SecurityProcessor securityProcessor = getSecurityProcessor(securityWatch);
        Object[] encryptParamResultArray = procParams(method, securityWatch, args, securityProcessor);
        try {
            if (void.class.equals(method.getReturnType())) {
                return pjp.proceed(args);
            }
            return procResult(method, securityWatch, pjp.proceed(args), securityProcessor);
        } finally {
            if (securityWatch.encryptRecovery()
                    && securityWatch.encryptRecovery()
                    && encryptParamResultArray != null
                    && encryptParamResultArray[0] != null
                    && encryptParamResultArray[1] != null) {
                List<SetterContextInfo> setterContexts = (List<SetterContextInfo>) encryptParamResultArray[0];
                Map<String, String> encryptedMap = (Map<String, String>) encryptParamResultArray[1];
                Map<String, String> recoveryMap = encryptedMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (oldValue, newValue) -> oldValue,
                                HashMap::new));
                setterContexts.stream().map(p -> p.getRecoverySetter()).filter(p -> p != null).forEach(p -> p.accept(recoveryMap));
            }
        }
    }

    SecurityProcessor getSecurityProcessor(SecurityWatch securityWatch) {
        String processorBeanName = securityWatch.processorBeanName();
        if (processorBeanName == null || processorBeanName.trim().length() == 0) {
            return defaultSecurityProcessor;
        }
        SecurityProcessor securityProcessor = applicationContext.getBean(processorBeanName, SecurityProcessor.class);
        if (securityProcessor == null) {
            throw new IllegalArgumentException("SecurityProcessor bean name is not found");
        }
        return securityProcessor;
    }

    Object[] procParams(Method method, SecurityWatch securityWatch, Object[] args, SecurityProcessor securityProcessor) throws Exception {
        if (args.length == 0 || !securityWatch.encrypt()) return null;
        List<SetterContextInfo> setterContexts = new ArrayList<>();
        Set<Object> processedObjects = new HashSet<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null || !Arrays.stream(parameterAnnotations[i]).parallel().anyMatch(annotation -> annotation instanceof SecurityParam)) continue;
            if (arg instanceof String) {
                String str = (String) arg;
                int finalI = i;
                setterContexts.add(new SetterContextInfo(p -> {
                    args[finalI] = p.get(str);
                }, p -> {
                    Object _value = args[finalI];
                    if (_value instanceof String) {
                        String value = p.get(_value);
                        if (value != null) {
                            args[finalI] = value;
                        }
                    }
                }, str));
            } else if (arg instanceof Securityable) {
                prepareSecurityable(method, arg, setterContexts, processedObjects, true);
            } else {
                prepareArrayAndCollections(method, arg, parameterAnnotations[i], setterContexts, processedObjects, true);
            }
        }
        if (setterContexts.size() > 0) {
            Set<String> originalValueSet = setterContexts.stream().flatMap(p -> p.getOriginals().stream()).collect(Collectors.toSet());
            Map<String, String> encryptedMap = securityProcessor.encrypt(method, originalValueSet);
            final Map<String, String>  _encryptedMap = encryptedMap;
            if (_encryptedMap != null) {
                setterContexts.stream().map(p -> p.getFeatureSetter()).toList().forEach(p -> p.accept(_encryptedMap));
                return new Object[]{setterContexts, new HashMap<>()};
            }
        }
        return new Object[]{setterContexts, new HashMap<>()};
    }

    Object procResult(Method method, SecurityWatch securityWatch, Object result, SecurityProcessor securityProcessor) throws Exception {
        if (result == null || !securityWatch.decrypt()) return result;
        List<SetterContextInfo> resultSetterContexts = new ArrayList<>();
        if (result instanceof String) {
            return securityProcessor.decrypt(method, (String) result);
        }
        if (result instanceof Securityable) {
            prepareSecurityable(method, result, resultSetterContexts, new HashSet<>(), false);
        } else {
            prepareArrayAndCollections(method, result, method.getAnnotations(), resultSetterContexts, new HashSet<>(), false);
        }
        if (!resultSetterContexts.isEmpty()) {
            Set<String> _originalValueSet = resultSetterContexts.stream().flatMap(p -> p.getOriginals().stream()).collect(Collectors.toSet());
            Map<String, String> _pmap = securityProcessor.decrypt(method, _originalValueSet);
            resultSetterContexts.stream().map(p -> p.getFeatureSetter()).toList().forEach(p -> p.accept(_pmap));
        }
        return result;
    }

    /**
     * 预处理Securityable对象特定字段
     *
     * @param method           代理方法
     * @param object           待处理的对象
     * @param setterContexts
     * @param processedObjects 处理中或已处理过的对象
     * @param enableRecovery   是否启用回滚模块
     * @throws Exception
     */
    void prepareSecurityable(Method method, Object object, List<SetterContextInfo> setterContexts, Set<Object> processedObjects, boolean enableRecovery) throws Exception {
        if (processedObjects.contains(object)) return;
        processedObjects.add(object);
        Class objectClass = object.getClass();
        List<Field> fieldList = Arrays.stream(objectClass.getDeclaredFields()).filter(field ->
                        field.isAnnotationPresent(SecurityField.class)
                                && !Modifier.isStatic(field.getModifiers())
                                && !Modifier.isFinal(field.getModifiers())
                                || Securityable.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        if (fieldList.size() == 0) return;
        for (int m = 0; m < fieldList.size(); m++) {
            Field field = fieldList.get(m);
            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null) continue;
            if (value instanceof String) {
                Consumer<Map<String, String>> setter = p -> {
                    try {
                        field.set(object, p.get(value));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                };
                if (enableRecovery) {
                    setterContexts.add(new SetterContextInfo(setter, p -> {
                        try {
                            String _value = p.get(value);
                            if (_value != null) {
                                field.set(object, _value);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }, (String) value));
                } else {
                    setterContexts.add(new SetterContextInfo(setter, (String) value));
                }
            } else if (value instanceof Securityable) {
                prepareSecurityable(method, value, setterContexts, processedObjects, enableRecovery);
            } else {
                prepareArrayAndCollections(method, value, field.getAnnotations(), setterContexts, processedObjects, enableRecovery);
            }
        }
    }

    /**
     * 预处理数组或集合对象
     *
     * @param method               代理方法
     * @param object               待处理的对象
     * @param parameterAnnotations 参数注解
     * @param setterContexts       待处理的上下文信息
     * @param processedObjects     处理中或已处理过的对象
     * @param enableRecovery       是否启用回滚模块
     * @throws Exception
     */
    void prepareArrayAndCollections(Method method, Object object, Annotation[] parameterAnnotations, List<SetterContextInfo> setterContexts, Set<Object> processedObjects, boolean enableRecovery) throws Exception {
        Class objectClass = object.getClass();
        if (objectClass.isArray()) {
            if (processedObjects.contains(object)) return;
            processedObjects.add(object);
            // 数组
            int length = Array.getLength(object);
            if (length == 0) return;
            Set<String> originalSet = new HashSet<>();
            for (int m = 0; m < length; m++) {
                Object value = Array.get(object, m);
                if (value == null) continue;
                if (value instanceof String) {
                    originalSet.add((String) value);
                } else if (value instanceof Securityable) {
                    prepareSecurityable(method, value, setterContexts, processedObjects, enableRecovery);
                } else if (value instanceof List
                        || value instanceof Set
                        || value instanceof Map) {
                    prepareArrayAndCollections(method, value, parameterAnnotations, setterContexts, processedObjects, enableRecovery);
                }
            }
            Consumer<Map<String, String>> setter = p -> {
                if (originalSet.size() == 0) return;
                for (int n = 0; n < length; n++) {
                    Object value = Array.get(object, n);
                    if (value == null || !(value instanceof String)) continue;
                    Array.set(object, n, p.get(value));
                }
            };
            if (enableRecovery) {
                setterContexts.add(new SetterContextInfo(setter, p -> {
                    for (int n = 0; n < length; n++) {
                        Object _value = Array.get(object, n);
                        if (_value == null || !(_value instanceof String)) continue;
                        String value = p.get(_value);
                        if (value != null) {
                            Array.set(object, n, value);
                        }
                    }
                }, originalSet));
            } else {
                setterContexts.add(new SetterContextInfo(setter, originalSet));
            }
        } else if (object instanceof List) {
            if (processedObjects.contains(object)) return;
            processedObjects.add(object);
            // 列表
            List list = (List) object;
            if (list.size() == 0) return;
            List<Object> copyList = new ArrayList<>(list.size());
            Set<String> originalSet = new HashSet<>();
            for (int m = 0; m < list.size(); m++) {
                Object value = list.get(m);
                copyList.add(value);
                if (value == null) continue;
                if (value instanceof String) {
                    originalSet.add((String) value);
                } else if (value instanceof Securityable) {
                    prepareSecurityable(method, value, setterContexts, processedObjects, enableRecovery);
                } else if (value instanceof List
                        || value instanceof Set
                        || value instanceof Map) {
                    prepareArrayAndCollections(method, value, parameterAnnotations, setterContexts, processedObjects, enableRecovery);
                }
            }
            Consumer<Map<String, String>> setter = p -> {
                if (originalSet.size() == 0) return;
                list.clear();
                for (int n = 0; n < copyList.size(); n++) {
                    Object value = copyList.get(n);
                    if (value instanceof String) {
                        list.add(p.get(value));
                        continue;
                    }
                    list.add(value);
                }
            };
            if (enableRecovery) {
                setterContexts.add(new SetterContextInfo(setter, p -> {
                    List<Object> _copyList = new ArrayList<Object>(list);
                    list.clear();
                    for (int n = 0; n < _copyList.size(); n++) {
                        Object _value = _copyList.get(n);
                        if (_value instanceof String) {
                            String value = p.get(_value);
                            if (value != null) {
                                list.add(value);
                                continue;
                            }
                        }
                        list.add(_value);
                    }
                }, originalSet));
            } else {
                setterContexts.add(new SetterContextInfo(setter, originalSet));
            }
        } else if (object instanceof Set) {
            if (processedObjects.contains(object)) return;
            processedObjects.add(object);
            // 集合
            Set set = (Set) object;
            if (set.size() == 0) return;
            Set<Object> copySet = new LinkedHashSet<>(set);
            Set<String> originalSet = new HashSet<>();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Object value = iterator.next();
                copySet.add(value);
                if (value == null) continue;
                if (value instanceof String) {
                    originalSet.add((String) value);
                } else if (value instanceof Securityable) {
                    prepareSecurityable(method, value, setterContexts, processedObjects, enableRecovery);
                } else if (value instanceof List
                        || value instanceof Set
                        || value instanceof Map) {
                    prepareArrayAndCollections(method, value, parameterAnnotations, setterContexts, processedObjects, enableRecovery);
                }
            }
            Consumer<Map<String, String>> setter = p -> {
                if (originalSet.size() == 0) return;
                set.clear();
                Iterator copyIterator = copySet.iterator();
                while (copyIterator.hasNext()) {
                    Object value = copyIterator.next();
                    if (value instanceof String) {
                        set.add(p.get(value));
                        continue;
                    }
                    set.add(value);
                }
            };
            if (enableRecovery) {
                setterContexts.add(new SetterContextInfo(setter, p -> {
                    Set<Object> _copySet = new LinkedHashSet<>(set);
                    set.clear();
                    Iterator _copyIterator = copySet.iterator();
                    while (_copyIterator.hasNext()) {
                        Object _value = _copyIterator.next();
                        if (_value instanceof String) {
                            String value = p.get(_value);
                            if (value != null) {
                                set.add(value);
                                continue;
                            }
                        }
                        set.add(_value);
                    }
                }, originalSet));
            } else {
                setterContexts.add(new SetterContextInfo(setter, originalSet));
            }
        } else if (object instanceof Map) {
            if (processedObjects.contains(object)) return;
            processedObjects.add(object);
            // Map
            Map map = (Map) object;
            if (map.size() == 0) return;
            SecurityMapKey securityMapKey = Arrays.stream(parameterAnnotations)
                    .filter(annotation -> annotation instanceof SecurityMapKey)
                    .map(annotation -> (SecurityMapKey) annotation)
                    .findFirst().orElse(null);
            if (securityMapKey == null || securityMapKey.value().length == 0) {
                return;
            }
            String[] encryptKeys = securityMapKey.value();
            Set<String> originalSet = new HashSet<>();
            for (int m = 0; m < encryptKeys.length; m++) {
                Object value = map.get(encryptKeys[m]);
                if (value == null) continue;
                if (value instanceof String) {
                    originalSet.add((String) value);
                } else if (value instanceof Securityable) {
                    prepareSecurityable(method, value, setterContexts, processedObjects, enableRecovery);
                } else if (value instanceof List
                        || value instanceof Set
                        || value instanceof Map) {
                    prepareArrayAndCollections(method, value, parameterAnnotations, setterContexts, processedObjects, enableRecovery);
                }
            }
            Consumer<Map<String, String>> setter = p -> {
                if (originalSet.size() == 0) return;
                for (int n = 0; n < encryptKeys.length; n++) {
                    String key = encryptKeys[n];
                    Object value = map.get(key);
                    if (value instanceof String) {
                        map.put(key, p.get(value));
                    }
                }
            };
            if (enableRecovery) {
                setterContexts.add(new SetterContextInfo(setter, p -> {
                    for (int n = 0; n < encryptKeys.length; n++) {
                        String key = encryptKeys[n];
                        Object _value = map.get(key);
                        if (_value instanceof String) {
                            String value = p.get(_value);
                            if (value != null) {
                                map.put(key, value);
                            }
                        }
                    }
                }, originalSet));
            } else {
                setterContexts.add(new SetterContextInfo(setter, originalSet));
            }
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
