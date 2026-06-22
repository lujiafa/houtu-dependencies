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
        buildSecurityParams(securityContext.getArgs(), securityContext.getParameterAnnotations(), getters, setters, new HashSet<>(), securityWatch.encryptMapKeys(), OperateType.ENCRYPT);
        IdentityHashMap<String, String> recoveryMap = new IdentityHashMap<>();
        if (!getters.isEmpty()) {
            Map<String, String> encryptedProcessMap = new ConcurrentHashMap<>(getters.size());
            List<String> origins = getters.stream().map(g -> g.get()).collect(Collectors.toList());
            origins.stream().forEach(o -> {
                if (encryptedProcessMap.get(o) == null) {
                    encryptedProcessMap.put(o, securityContext.getSecurityProcessor().encrypt(securityContext.getMethod(), o));
                }
            });
            IdentityHashMap<String, String> encryptedMap = new IdentityHashMap<>();
            origins.stream().forEach(o -> {
                if (!encryptedMap.containsKey(o)) {
                    String encrypted = new String(encryptedProcessMap.get(o));
                    encryptedMap.put(o, encrypted);
                }
            });
            setters.stream().forEach(s -> s.set(encryptedMap));
            encryptedMap.entrySet().forEach(e -> recoveryMap.put(e.getValue(), e.getKey()));
        }
        securityContext.setRecoveryMap(recoveryMap);
    }

    void recoveryParams(SecurityContext securityContext) {
        List<SecuritySetter> setters = new ArrayList<>();
        buildSecurityParams(securityContext.getArgs(), securityContext.getParameterAnnotations(), null, setters, securityContext.getRecoveryAndDecryptProcessedSet(), null, OperateType.RECOVERY);
        setters.stream().forEach(s -> s.set(securityContext.getRecoveryMap()));
    }

    void buildSecurityParams(Object[] args, Annotation[][] parameterAnnotations, List<Supplier<String>> getters, List<SecuritySetter> setters, Set<Object> processedSet, String[] mapKeys, OperateType operateType) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null
                    || (!Arrays.stream(parameterAnnotations[i]).anyMatch(annotation -> annotation instanceof SecurityParam) && !(arg instanceof SecurityObject)))
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
                build(arg, getters, setters, processedSet, mapKeys, operateType);
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
            return securityContext.getSecurityProcessor().decrypt(securityContext.getMethod(), (String) result);
        }
        List<Supplier<String>> getters = new ArrayList<>();
        List<SecuritySetter> setters = new ArrayList<>();
        build(result, getters, setters, securityContext.getRecoveryAndDecryptProcessedSet(), securityContext.getSecurityWatch().decryptMapKeys(), OperateType.DECRYPT);
        if (!getters.isEmpty()) {
            IdentityHashMap<String, String> recoveryMap = securityContext.getRecoveryMap();
            Map<String, String> decryptProcessMap = recoveryMap == null ? new ConcurrentHashMap<>(getters.size()) : new ConcurrentHashMap<>(recoveryMap);
            List<String> decrypts = getters.stream().map(g -> g.get()).collect(Collectors.toList());
            decrypts.stream().forEach(o -> {
                if (decryptProcessMap.get(o) == null) {
                    decryptProcessMap.put(o, securityContext.getSecurityProcessor().decrypt(securityContext.getMethod(), o));
                }
            });
            IdentityHashMap<String, String> decryptionMap = recoveryMap == null ? new IdentityHashMap<>() : new IdentityHashMap<>(recoveryMap);
            decrypts.stream().forEach(o -> {
                if (!decryptionMap.containsKey(o)) {
                    decryptionMap.put(o, decryptProcessMap.get(o));
                }
            });
            setters.stream().forEach(s -> s.set(decryptionMap));
        }
        return result;
    }

    void build(Object object, List<Supplier<String>> getters, List<SecuritySetter> setters, Set<Object> processedSet, String[] mapKeys, OperateType operateType) {
        if (object == null || processedSet.contains(object)) return;
        if (object instanceof SecurityObject) {
            List<Field> fieldList = new ArrayList<>();
            Class<?> clazz = object.getClass();
            while (clazz != null && SecurityObject.class.isAssignableFrom(clazz)) {
                Arrays.stream(clazz.getDeclaredFields())
                        .filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))
                        .forEach(fieldList::add);
                clazz = clazz.getSuperclass();
            }
            if (fieldList.size() == 0) return;
            processedSet.add(object);
            try {
                for (int i = 0; i < fieldList.size(); i++) {
                    Field field = fieldList.get(i);
                    if (!OperateType.RECOVERY.equals(operateType)
                            && !field.isAnnotationPresent(SecurityParam.class)
                            && !SecurityObject.class.isAssignableFrom(field.getType())) {
                        continue;
                    }
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
                        build(value, getters, setters, processedSet, mapKeys, operateType);
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
                    build(value, getters, setters, processedSet, mapKeys, operateType);
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
            for (Object value : origins) {
                if (!(value instanceof String)) {
                    build(value, getters, setters, processedSet, mapKeys, operateType);
                }
            }
            if (setters != null) {
                setters.add((m) -> {
                    collection.clear();
                    for (Object value : origins) {
                        if (value instanceof String) {
                            String _value = (String) value;
                            String newValue = m.get(_value);
                            collection.add(newValue != null ? newValue : _value);
                        } else {
                            collection.add(value);
                        }
                    }
                });
            }
        } else if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            if (map.isEmpty() || isImmutable(object.getClass())) return;
            if (operateType != OperateType.RECOVERY && (mapKeys == null || mapKeys.length == 0)) return;
            processedSet.add(object);
            Map<String, Object> origins = new LinkedHashMap<>(map);
            Set<Map.Entry<String, Object>> entries = origins.entrySet();
            Set<String> filterMapKeysSet = new HashSet<>(operateType == OperateType.RECOVERY ? origins.keySet() : Arrays.asList(mapKeys));
            if (getters != null) {
                entries.stream().filter(e -> e.getValue() instanceof String && filterMapKeysSet.contains(e.getKey())).forEach(e -> getters.add(() -> (String) e.getValue()));
            }
            for (Map.Entry<String, Object> entry : entries) {
                Object value = entry.getValue();
                if (!(value instanceof String)
                        && (filterMapKeysSet.contains(entry.getKey()) || value instanceof SecurityObject)) {
                    build(value, getters, setters, processedSet, mapKeys, operateType);
                }
            }
            if (setters != null) {
                setters.add(m -> {
                    map.clear();
                    for (Map.Entry<String, Object> entry : entries) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (value instanceof String) {
                            String _value = (String) value;
                            if (filterMapKeysSet.contains(key)) {
                                String newValue = m.get(_value);
                                map.put(key, newValue != null ? newValue : _value);
                            } else {
                                map.put(key, _value);
                            }
                        } else {
                            map.put(key, value);
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
        DECRYPT,
        RECOVERY;
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
