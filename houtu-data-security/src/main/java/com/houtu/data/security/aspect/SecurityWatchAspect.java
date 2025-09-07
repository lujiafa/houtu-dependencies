package com.houtu.data.security.aspect;

import com.houtu.core.context.SpringApplicationContext;
import com.houtu.data.security.annotation.SecurityParam;
import com.houtu.data.security.annotation.SecurityWatch;
import com.houtu.data.security.handler.SecurityProcessor;
import com.houtu.data.security.handler.SecuritySetter;
import com.houtu.data.security.support.SecuritySupport;
import com.houtu.util.common.AnnotationUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author jonlu
 * @date 2019年5月29日
 */
@Aspect
public class SecurityWatchAspect implements Ordered {

    private SecurityProcessor securityProcessor;

    public SecurityWatchAspect(SecurityProcessor securityProcessor) {
        this.securityProcessor = securityProcessor;
    }

    @Pointcut("@within(com.houtu.data.security.annotation.SecurityWatch) || @annotation(com.houtu.data.security.annotation.SecurityWatch)")
    public void dataSecurityWatchPointcut() {
    }

    //    @Around("daoWatchPointcut() || execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.*(..))")
    @Around("dataSecurityWatchPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        SecurityWatch securityWatch = AnnotationUtils.getAnnotationByPriorityMethod(method, SecurityWatch.class);
        SecurityProcessor securityProcessor = getSecurityProcessor(securityWatch);
        IdentityHashMap<String, String> recoveryMap = null;
        Set<Object> recoveryAndDecryptSet = null;
        if (args.length > 0 && securityWatch.encrypt()) {
            recoveryMap = encryptParams(method, securityWatch, securityProcessor, args);
        }
        try {
            Object result = pjp.proceed(args);
            if (result == null || void.class.equals(method.getReturnType()) || !securityWatch.decrypt()) {
                return result;
            }
            return decryptResult(method, securityWatch, securityProcessor, recoveryMap, result, recoveryAndDecryptSet = new HashSet<>());
        } finally {
            if (recoveryMap != null && !recoveryMap.isEmpty()) {
                recoveryParams(method, args, recoveryMap, recoveryAndDecryptSet);
            }
        }
    }

    SecurityProcessor getSecurityProcessor(SecurityWatch securityWatch) {
        SecurityProcessor securityProcessor;
        String processorBeanName = securityWatch.processorBeanName();
        if (StringUtils.hasLength(processorBeanName)) {
            securityProcessor = SpringApplicationContext.getBean(processorBeanName, securityWatch.processorClass());
        } else {
            securityProcessor = this.securityProcessor;
        }
        if (securityProcessor == null) {
            throw new IllegalArgumentException("SecurityProcessor bean name is not found");
        }
        return securityProcessor;
    }


    IdentityHashMap<String, String> encryptParams(Method method, SecurityWatch securityWatch, SecurityProcessor securityProcessor, Object[] args) {
        List<Supplier<String>> getters = new ArrayList<>();
        List<SecuritySetter> setters = new ArrayList<>();
        buildSecurityParams(args, method.getParameterAnnotations(), getters, setters, new HashSet<>(), securityWatch.encryptMapKeys());
        IdentityHashMap<String, String> recoveryMap = new IdentityHashMap<>();
        if (!getters.isEmpty()) {
            Map<String, String> encryptedProcessMap = new ConcurrentHashMap<>(getters.size());
            List<String> origins = getters.stream().map(g -> g.get()).toList();
            origins.parallelStream().forEach(o -> {
                if (encryptedProcessMap.get(o) == null) {
                    encryptedProcessMap.put(o, securityProcessor.encrypt(method, o));
                }
            });
            IdentityHashMap<String, String> encryptedMap = new IdentityHashMap<>();
            origins.stream().forEach(o -> {
                if (!encryptedMap.containsKey(o)) {
                    String encrypted = new String(encryptedProcessMap.get(o));
                    encryptedMap.put(o, encrypted);
                }
            });
            setters.parallelStream().forEach(s -> s.set(encryptedMap));
            encryptedMap.entrySet().forEach(e -> recoveryMap.put(e.getValue(), e.getKey()));
        }
        return recoveryMap;
    }

    void recoveryParams(Method method, Object[] args, IdentityHashMap<String, String> recoveryMap, Set<Object> recoveryAndDecryptSet) {
        List<SecuritySetter> setters = new ArrayList<>();
        buildSecurityParams(args, method.getParameterAnnotations(), null, setters, recoveryAndDecryptSet == null ? new HashSet<>() : recoveryAndDecryptSet, null);
        setters.parallelStream().forEach(s -> s.set(recoveryMap));
    }


    void buildSecurityParams(Object[] args, Annotation[][] parameterAnnotations, List<Supplier<String>> getters, List<SecuritySetter> setters, Set<Object> processedSet, String[] mapKeys) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null
                    || (!Arrays.stream(parameterAnnotations[i]).parallel().anyMatch(annotation -> annotation instanceof SecurityParam) && !(arg instanceof SecuritySupport)))
                continue;
            if (arg instanceof String _arg) {
                if (getters != null) {
                    getters.add(() -> _arg);
                }
                if (setters != null) {
                    int finalI = i;
                    setters.add(m -> args[finalI] = m.get(args[finalI]));
                }
            } else {
                build(arg, getters, setters, processedSet, mapKeys);
            }
        }
    }

    Object decryptResult(Method method, SecurityWatch securityWatch, SecurityProcessor securityProcessor, IdentityHashMap<String, String> recoveryMap, Object result, Set<Object> recoveryAndDecryptSet) {
        if (result instanceof String _result) {
            if (recoveryMap != null) {
                String original = recoveryMap.get(_result);
                if (original != null) {
                    return original;
                }
            }
            return securityProcessor.decrypt(method, _result);
        }
        List<Supplier<String>> getters = new ArrayList<>();
        List<SecuritySetter> setters = new ArrayList<>();
        build(result, getters, setters, recoveryAndDecryptSet, securityWatch.decryptMapKeys());
        if (!getters.isEmpty()) {
            Map<String, String> decryptProcessMap = recoveryMap == null ? new ConcurrentHashMap<>(getters.size()) : new ConcurrentHashMap<>(recoveryMap);
            List<String> encrypts = getters.stream().map(g -> g.get()).toList();
            encrypts.parallelStream().forEach(o -> {
                if (decryptProcessMap.get(o) == null) {
                    decryptProcessMap.put(o, securityProcessor.decrypt(method, o));
                }
            });
            IdentityHashMap<String, String> decryptionMap = recoveryMap == null ? new IdentityHashMap<>() : new IdentityHashMap<>(recoveryMap);
            encrypts.stream().forEach(o -> {
                if (!decryptionMap.containsKey(o)) {
                    decryptionMap.put(o, decryptProcessMap.get(o));
                }
            });
            setters.parallelStream().forEach(s -> s.set(decryptionMap));
        }
        return result;
    }

    void build(Object object, List<Supplier<String>> getters, List<SecuritySetter> setters, Set<Object> processedSet, String[] mapKeys) {
        if (object == null || processedSet.contains(object)) return;
        if (object instanceof SecuritySupport) {
            List<Field> fieldList = Arrays.stream(object.getClass().getDeclaredFields()).filter(field ->
                            field.isAnnotationPresent(SecurityParam.class)
                                    && !Modifier.isStatic(field.getModifiers())
                                    && !Modifier.isFinal(field.getModifiers())
                                    || SecuritySupport.class.isAssignableFrom(field.getType()))
                    .collect(Collectors.toList());
            if (fieldList.size() == 0) return;
            processedSet.add(object);
            try {
                for (int i = 0; i < fieldList.size(); i++) {
                    Field field = fieldList.get(i);
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value == null) continue;
                    if (value instanceof String _value) {
                        if (getters != null) {
                            getters.add(() -> _value);
                        }
                        if (setters != null) {
                            setters.add(m -> {
                                try {
                                    String newValue = m.get(_value);
                                    if (newValue != null) {
                                        field.set(object, newValue);
                                    }
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e.getMessage(), e);
                                }
                            });
                        }
                    } else {
                        build(value, getters, setters, processedSet, mapKeys);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            if (length == 0) return;
            processedSet.add(object);
            Object[] origins = new Object[length];
            System.arraycopy(object, 0, origins, 0, length);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(object, i);
                if (value == null) continue;
                if (value instanceof String _value) {
                    int finalI = i;
                    if (getters != null) {
                        getters.add(() -> _value);
                    }
                    if (setters != null) {
                        setters.add(m -> {
                            String newValue = m.get(_value);
                            if (newValue != null) {
                                Array.set(object, finalI, newValue);
                            }
                        });
                    }
                }else {
                    build(value, getters, setters, processedSet, mapKeys);
                }
            }
        } else if (object instanceof List || object instanceof Set) {
            Collection collection = (Collection) object;
            if (collection.isEmpty() || isImmutable(object.getClass())) return;
            processedSet.add(object);
            ArrayList<Object> origins = new ArrayList<Object>(collection);
            if (getters != null) {
                origins.stream().filter(v -> v instanceof String).forEach(v -> getters.add(() -> (String) v));
            }
            if (setters != null) {
                setters.add((m) -> {
                    collection.clear();
                    for (int i = 0; i < origins.size(); i++) {
                        Object value = origins.get(i);
                        if (value instanceof String _value) {
                            String newValue = m.get(_value);
                            if (newValue == null) {
                                collection.add(_value);
                            } else {
                                collection.add(newValue);
                            }
                        }else {
                            collection.add(value);
                            build(value, getters, setters, processedSet, mapKeys);
                        }
                    }
                });
            }
        } else if (object instanceof Map map) {
            if (map.isEmpty() || isImmutable(object.getClass())) return;
            processedSet.add(object);
            Map<String, Object> origins = new LinkedHashMap<>(map);
            Set<Map.Entry<String, Object>> entries = origins.entrySet();
            if (getters != null) {
                entries.stream().filter(e -> e.getValue() instanceof String).forEach(e -> getters.add(() -> (String) e.getValue()));
            }
            if (setters != null) {
                Set<String> encryptMapKeysSet = mapKeys == null || mapKeys.length == 0 ? map.keySet() : new HashSet<>(Arrays.asList(mapKeys));
                setters.add(m -> {
                    map.clear();
                    for (Map.Entry<String, Object> entry : entries) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (value instanceof String _value) {
                            if (encryptMapKeysSet.contains(key)) {
                                String newValue = m.get(_value);
                                if (newValue == null) {
                                    m.put(key, _value);
                                } else {
                                    map.put(key, newValue);
                                }
                            } else {
                                map.put(key, _value);
                            }
                        } else {
                            map.put(key, value);
                            if (encryptMapKeysSet.contains(key) || value instanceof SecuritySupport) {
                                build(value, getters, setters, processedSet, mapKeys);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 判断集合是否为不可变类
     *
     * @param clazz 集合类
     * @return 是否为不可变类 true-不可变 false-可变
     */
    boolean isImmutable(Class clazz) {
        String clazzName = clazz.getName();
        return clazzName.contains("Immutable") || clazzName.contains("Unmodifiable");
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    static enum OperateType {
        ENCRYPT,
        DECRYPT;
    }


    static class SecurityContext {
        protected OperateType operateType;
        protected Method method;
        protected SecurityWatch securityWatch;
        protected SecurityProcessor securityProcessor;
        protected Set<Object> processedSet = new HashSet<>();
        protected List<Supplier<String>> getters = new ArrayList<>();
        protected List<SecuritySetter> setters = new ArrayList<>();

        public SecurityContext(OperateType operateType, Method method, SecurityWatch securityWatch, SecurityProcessor securityProcessor, Set<Object> processedSet) {
            this.operateType = operateType;
            this.method = method;
            this.securityWatch = securityWatch;
            this.securityProcessor = securityProcessor;
            this.processedSet = processedSet == null ? new HashSet<>() : processedSet;
        }

        public OperateType getOperateType() {
            return operateType;
        }

        public Method getMethod() {
            return method;
        }

        public SecurityWatch getSecurityWatch() {
            return securityWatch;
        }

        public SecurityProcessor getSecurityProcessor() {
            return securityProcessor;
        }

        public boolean isProcessed(Object object) {
            return processedSet.contains(object);
        }

        public void addProcessed(Object object) {
            processedSet.add(object);
        }

        public Set<Object> getProcessedSet() {
            return processedSet;
        }

        public void addGetter(Supplier<String> getter) {
            getters.add(getter);
        }

        public void addSetter(SecuritySetter setter) {
            setters.add(setter);
        }

        public List<Supplier<String>> getGetters() {
            return getters;
        }

        public List<SecuritySetter> getSetter() {
            return setters;
        }
    }

}
