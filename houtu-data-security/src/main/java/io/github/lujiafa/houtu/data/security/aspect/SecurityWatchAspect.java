package io.github.lujiafa.houtu.data.security.aspect;

import io.github.lujiafa.houtu.core.context.SpringApplicationContext;
import io.github.lujiafa.houtu.data.security.annotation.SecurityParam;
import io.github.lujiafa.houtu.data.security.annotation.SecurityWatch;
import io.github.lujiafa.houtu.data.security.handler.SecurityProcessor;
import io.github.lujiafa.houtu.data.security.handler.SecuritySetter;
import io.github.lujiafa.houtu.data.security.support.SecurityObject;
import io.github.lujiafa.houtu.util.common.AnnotationUtils;
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

//    @Pointcut("@within(io.github.lujiafa.houtu.annotation.security.data.houtu.SecurityWatch) " +
//            "|| @annotation(io.github.lujiafa.houtu.annotation.security.data.houtu.SecurityWatch) " +
//            "|| execution(* (@io.github.lujiafa.houtu.annotation.security.data.houtu.SecurityWatch *).*(..)) " +
//            "|| within(@io.github.lujiafa.houtu.annotation.security.data.houtu.SecurityWatch *)")
    @Pointcut("@within(io.github.lujiafa.houtu.data.security.annotation.SecurityWatch) || @annotation(io.github.lujiafa.houtu.data.security.annotation.SecurityWatch)")
    public void dataSecurityWatchPointcut() {}

    @Around("dataSecurityWatchPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        SecurityWatch securityWatch = AnnotationUtils.getAnnotationByPriorityMethod(method, SecurityWatch.class);
        Object[] args = pjp.getArgs();
        SecurityProcessor securityProcessor = getSecurityProcessor(securityWatch);
        SecurityContext securityContext = new SecurityContext(method, args, securityWatch, securityProcessor);
        if (args.length > 0 && securityWatch.encrypt()) {
            encryptParams(securityContext);
        }
        try {
            Object result = pjp.proceed(args);
            if (result == null || void.class.equals(method.getReturnType()) || !securityWatch.decrypt()) {
                return result;
            }
            return decryptResult(securityContext, result);
        } finally {
            if (securityContext.getRecoveryMap() != null && !securityContext.getRecoveryMap().isEmpty()) {
                recoveryParams(securityContext);
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

    void encryptParams(SecurityContext securityContext) {
        List<Supplier<String>> getters = new ArrayList<>();
        List<SecuritySetter> setters = new ArrayList<>();
        SecurityWatch securityWatch = securityContext.getSecurityWatch();
        buildSecurityParams(securityContext.getArgs(), securityContext.getParameterAnnotations(), getters, setters, new HashSet<>(), securityWatch.encryptMapKeys());
        IdentityHashMap<String, String> recoveryMap = new IdentityHashMap<>();
        if (!getters.isEmpty()) {
            Map<String, String> encryptedProcessMap = new ConcurrentHashMap<>(getters.size());
            List<String> origins = getters.stream().map(g -> g.get()).collect(Collectors.toList());
            origins.parallelStream().forEach(o -> {
                if (encryptedProcessMap.get(o) == null) {
                    encryptedProcessMap.put(o, securityProcessor.encrypt(securityContext.getMethod(), o));
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
        securityContext.setRecoveryMap(recoveryMap);
    }

    void recoveryParams(SecurityContext securityContext) {
        List<SecuritySetter> setters = new ArrayList<>();
        buildSecurityParams(securityContext.getArgs(), securityContext.getParameterAnnotations(), null, setters, securityContext.getRecoveryAndDecryptProcessedSet(), null);
        setters.parallelStream().forEach(s -> s.set(securityContext.getRecoveryMap()));
    }

    void buildSecurityParams(Object[] args, Annotation[][] parameterAnnotations, List<Supplier<String>> getters, List<SecuritySetter> setters, Set<Object> processedSet, String[] mapKeys) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null
                    || (!Arrays.stream(parameterAnnotations[i]).parallel().anyMatch(annotation -> annotation instanceof SecurityParam) && !(arg instanceof SecurityObject)))
                continue;
            if (arg instanceof String) {
                if (getters != null) {
                    getters.add(() -> (String) arg);
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

    Object decryptResult(SecurityContext securityContext, Object result) {
        if (result instanceof String) {
            IdentityHashMap<String, String> recoveryMap = securityContext.getRecoveryMap();
            if (recoveryMap != null) {
                String original = securityContext.getRecoveryMap().get((String) result);
                if (original != null) {
                    return original;
                }
            }
            return securityProcessor.decrypt(securityContext.getMethod(), (String) result);
        }
        List<Supplier<String>> getters = new ArrayList<>();
        List<SecuritySetter> setters = new ArrayList<>();
        build(result, getters, setters, securityContext.getRecoveryAndDecryptProcessedSet(), securityContext.getSecurityWatch().decryptMapKeys());
        if (!getters.isEmpty()) {
            IdentityHashMap<String, String> recoveryMap = securityContext.getRecoveryMap();
            Map<String, String> decryptProcessMap = recoveryMap == null ? new ConcurrentHashMap<>(getters.size()) : new ConcurrentHashMap<>(recoveryMap);
            List<String> encrypts = getters.stream().map(g -> g.get()).collect(Collectors.toList());
            encrypts.parallelStream().forEach(o -> {
                if (decryptProcessMap.get(o) == null) {
                    decryptProcessMap.put(o, securityProcessor.decrypt(securityContext.getMethod(), o));
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
        if (object instanceof SecurityObject) {
            List<Field> fieldList = Arrays.stream(object.getClass().getDeclaredFields()).filter(field ->
                            field.isAnnotationPresent(SecurityParam.class)
                                    && !Modifier.isStatic(field.getModifiers())
                                    && !Modifier.isFinal(field.getModifiers())
                                    || SecurityObject.class.isAssignableFrom(field.getType()))
                    .collect(Collectors.toList());
            if (fieldList.size() == 0) return;
            processedSet.add(object);
            try {
                for (int i = 0; i < fieldList.size(); i++) {
                    Field field = fieldList.get(i);
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value == null) continue;
                    if (value instanceof String) {
                        if (getters != null) {
                            getters.add(() -> (String) value);
                        }
                        if (setters != null) {
                            setters.add(m -> {
                                try {
                                    String newValue = m.get((String) value);
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
                if (value instanceof String) {
                    int finalI = i;
                    if (getters != null) {
                        getters.add(() -> (String) value);
                    }
                    if (setters != null) {
                        setters.add(m -> {
                            String newValue = m.get((String) value);
                            if (newValue != null) {
                                Array.set(object, finalI, newValue);
                            }
                        });
                    }
                } else {
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
                        if (value instanceof String) {
                            String newValue = m.get((String) value);
                            if (newValue == null) {
                                collection.add((String) value);
                            } else {
                                collection.add(newValue);
                            }
                        } else {
                            collection.add(value);
                            build(value, getters, setters, processedSet, mapKeys);
                        }
                    }
                });
            }
        } else if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
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
                        if (value instanceof String) {
                            if (encryptMapKeysSet.contains(key)) {
                                String newValue = m.get((String) value);
                                if (newValue == null) {
                                    map.put(key, (String) value);
                                } else {
                                    map.put(key, newValue);
                                }
                            } else {
                                map.put(key, (String) value);
                            }
                        } else {
                            map.put(key, value);
                            if (encryptMapKeysSet.contains(key) || value instanceof SecurityObject) {
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
        protected Method method;
        private Object[] args;
        protected SecurityWatch securityWatch;
        protected SecurityProcessor securityProcessor;
        protected Annotation[][] parameterAnnotations;

        private IdentityHashMap<String, String> recoveryMap;

        private Set<Object> recoveryAndDecryptProcessedSet;

        public SecurityContext(Method method, Object[] args, SecurityWatch securityWatch, SecurityProcessor securityProcessor) {
            this.method = method;
            this.args = args;
            this.securityWatch = securityWatch;
            this.securityProcessor = securityProcessor;
        }

        public Method getMethod() {
            return this.method;
        }

        public Object[] getArgs() {
            return args;
        }

        public SecurityWatch getSecurityWatch() {
            return this.securityWatch;
        }

        public SecurityProcessor getSecurityProcessor() {
            return this.securityProcessor;
        }

        public Annotation[][] getParameterAnnotations() {
            if (this.parameterAnnotations == null) {
                this.parameterAnnotations = method.getParameterAnnotations();
            }
            return this.parameterAnnotations;
        }

        public IdentityHashMap<String, String> getRecoveryMap() {
            return recoveryMap;
        }

        public void setRecoveryMap(IdentityHashMap<String, String> recoveryMap) {
            this.recoveryMap = recoveryMap;
        }

        public Set<Object> getRecoveryAndDecryptProcessedSet() {
            if (this.recoveryAndDecryptProcessedSet == null)  {
                this.recoveryAndDecryptProcessedSet = new HashSet<>();
            }
            return this.recoveryAndDecryptProcessedSet;
        }

        public void setRecoveryAndDecryptProcessedSet(Set<Object> recoveryAndDecryptProcessedSet) {
            this.recoveryAndDecryptProcessedSet = recoveryAndDecryptProcessedSet;
        }
    }

}
