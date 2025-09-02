package com.houtu.data.security.aspect;

import com.houtu.core.context.SpringApplicationContext;
import com.houtu.data.security.annotation.SecurityParam;
import com.houtu.data.security.annotation.SecurityWatch;
import com.houtu.data.security.handler.SecurityDecrypt;
import com.houtu.data.security.handler.SecurityEncrypt;
import com.houtu.data.security.handler.SecurityProcessor;
import com.houtu.data.security.handler.SecurityRecovery;
import com.houtu.data.security.support.Securityable;
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
    public void daoWatchPointcut() {
    }

    //    @Around("daoWatchPointcut() || execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.*(..))")
    @Around("daoWatchPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        SecurityWatch securityWatch = AnnotationUtils.getAnnotationByPriorityMethod(method, SecurityWatch.class);
        SecurityProcessor securityProcessor = getSecurityProcessor(securityWatch);
        SecurityParamsContext securityContext = null;
        if (args.length > 0 && securityWatch.encrypt()) {
            buildSecurityParams(securityContext = new SecurityParamsContext(method, securityWatch, securityProcessor), args)
                    .getEncrypts().parallelStream().forEach(s -> s.encrypt());
        }
        try {
            Object result = pjp.proceed(args);
            if (result == null || void.class.equals(method.getReturnType()) || !securityWatch.decrypt()) {
                return result;
            }
            return decryptResult(new SecurityResultContext(method, securityWatch, securityProcessor, securityContext), pjp.proceed(args));
        } finally {
            if (securityContext != null
                    && !securityContext.getRecoveries().isEmpty()) {
                securityContext.getRecoveries().parallelStream().forEach(r -> r.recovery());
            }
        }
    }

    SecurityProcessor getSecurityProcessor(SecurityWatch securityWatch) {
        SecurityProcessor securityProcessor = null;
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


    SecurityParamsContext buildSecurityParams(SecurityParamsContext securityContext, Object[] args) {
        Annotation[][] parameterAnnotations = securityContext.getMethod().getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null
                    || (!Arrays.stream(parameterAnnotations[i]).parallel().anyMatch(annotation -> annotation instanceof SecurityParam) && !(arg instanceof Securityable)))
                continue;
            if (arg instanceof String _arg) {
                int finalI = i;
                SecurityEncrypt setter = () -> Array.set(args, finalI, encrypt(securityContext, _arg));
                SecurityRecovery recovery = () -> Array.set(args, finalI, _arg);
                securityContext.add(setter, recovery);
            } else if (arg instanceof Securityable _arg) {
                buildSecurityableSecurityParams(securityContext, _arg);
            } else {
                buildArrayAndCollectionsSecurityParams(securityContext, arg);
            }
        }
        return securityContext;
    }

    void buildSecurityableSecurityParams(SecurityParamsContext securityContext, Securityable securityable) {
        if (securityContext.isProcessed(securityable)) return;
        List<Field> fieldList = Arrays.stream(securityable.getClass().getDeclaredFields()).filter(field ->
                        field.isAnnotationPresent(SecurityParam.class)
                                && !Modifier.isStatic(field.getModifiers())
                                && !Modifier.isFinal(field.getModifiers())
                                || Securityable.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        if (fieldList.size() == 0) return;
        securityContext.addProcessed(securityable);
        try {
            for (int i = 0; i < fieldList.size(); i++) {
                Field field = fieldList.get(i);
                field.setAccessible(true);
                Object value = field.get(securityable);
                if (value == null) continue;
                if (value instanceof String _value) {
                    SecurityEncrypt setter = () -> {
                        try {
                            field.set(securityable, encrypt(securityContext, _value));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    };
                    SecurityRecovery recovery = () -> {
                        try {
                            field.set(securityable, _value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    };
                    securityContext.add(setter, recovery);
                } else if (value instanceof Securityable _value) {
                    buildSecurityableSecurityParams(securityContext, _value);
                } else {
                    buildArrayAndCollectionsSecurityParams(securityContext, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void buildArrayAndCollectionsSecurityParams(SecurityParamsContext securityContext, Object object) {
        if (securityContext.isProcessed(object)) return;
        if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            if (length == 0) return;
            securityContext.addProcessed(object);
            Object[] origins = new Object[length];
            System.arraycopy(object, 0, origins, 0, length);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(object, i);
                if (value == null) continue;
                if (value instanceof String _value) {
                    int finalI = i;
                    SecurityEncrypt setter = () -> Array.set(object, finalI, encrypt(securityContext, _value));
                    securityContext.addEncrypt(setter);
                } else if (value instanceof Securityable _value) {
                    buildSecurityableSecurityParams(securityContext, _value);
                } else {
                    buildArrayAndCollectionsSecurityParams(securityContext, value);
                }
            }
            SecurityRecovery recovery = () -> System.arraycopy(origins, 0, object, 0, length);
            securityContext.addRecovery(recovery);
        } else if (object instanceof List || object instanceof Set) {
            Collection collection = (Collection) object;
            if (collection.isEmpty() || isImmutable(object.getClass())) return;
            securityContext.addProcessed(object);
            ArrayList<Object> origins = new ArrayList<Object>(collection);
            SecurityEncrypt setter = () -> {
                collection.clear();
                for (int i = 0; i < origins.size(); i++) {
                    Object value = origins.get(i);
                    if (value instanceof String _value) {
                        collection.add(encrypt(securityContext, _value));
                    } else if (value instanceof Securityable _value) {
                        collection.add(_value);
                        buildSecurityableSecurityParams(securityContext, _value);
                    } else {
                        collection.add(value);
                        buildArrayAndCollectionsSecurityParams(securityContext, value);
                    }
                }
            };
            SecurityRecovery recovery = () -> {
                collection.clear();
                collection.addAll(origins);
            };
            securityContext.add(setter, recovery);
        } else if (object instanceof Map map) {
            if (map.isEmpty() || isImmutable(object.getClass())) return;
            String[] encryptMapKeys = securityContext.getSecurityWatch().encryptMapKeys();
            securityContext.addProcessed(object);
            Map<String, Object> origins = new LinkedHashMap<>(map);
            Set<String> encryptMapKeysSet = encryptMapKeys == null || encryptMapKeys.length == 0 ? map.keySet() : new HashSet<>(Arrays.asList(encryptMapKeys));
            SecurityEncrypt setter = () -> {
                map.clear();
                for (Map.Entry<String, Object> entry : origins.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String _value) {
                        if (encryptMapKeysSet.contains(key)) {
                            map.put(key, encrypt(securityContext, _value));
                        } else {
                            map.put(key, _value);
                        }
                    } else if (value instanceof Securityable _value) {
                        map.put(key, _value);
                        buildSecurityableSecurityParams(securityContext, _value);
                    } else {
                        map.put(key, value);
                        if (encryptMapKeysSet.contains(key)) {
                            buildArrayAndCollectionsSecurityParams(securityContext, value);
                        }
                    }
                }
            };
            SecurityRecovery recovery = () -> {
                map.clear();
                map.putAll(origins);
            };
            securityContext.add(setter, recovery);
        }
    }

    String encrypt(SecurityParamsContext securityContext, String original) {
        String encrypted = securityContext.getSecurityProcessor().encrypt(securityContext.getMethod(), original);
        if (encrypted != null) {
            securityContext.getEncryptedMap().put(original, encrypted = new String(encrypted));
        }
        return encrypted;
    }

    Object decryptResult(SecurityResultContext securityContext, Object result) {
        if (result instanceof String _result) {
            Optional<String> optional = securityContext.getSecurityParamsContext().getEncryptedMap().entrySet().parallelStream().filter(entry -> entry.getValue() != null && entry.getValue() == _result).map(entry -> entry.getKey()).findFirst();
            if (optional.isPresent()) {
                return optional.get();
            }
            return securityContext.getSecurityProcessor().decrypt(securityContext.getMethod(), _result);
        }
        if (result instanceof Securityable _result) {
            buildSecurityableResult(securityContext, _result);
        } else {
            buildArrayAndCollectionsResult(securityContext, result);
        }
        securityContext.getDecrypts().parallelStream().forEach(p -> p.decrypt());
        return result;
    }

    void buildSecurityableResult(SecurityResultContext securityContext, Securityable securityable) {
        if (securityContext.isProcessed(securityable)) return;
        List<Field> fieldList = Arrays.stream(securityable.getClass().getDeclaredFields()).filter(field ->
                        field.isAnnotationPresent(SecurityParam.class)
                                && !Modifier.isStatic(field.getModifiers())
                                && !Modifier.isFinal(field.getModifiers())
                                || Securityable.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        if (fieldList.size() == 0) return;
        securityContext.addProcessed(securityable);
        try {
            for (int i = 0; i < fieldList.size(); i++) {
                Field field = fieldList.get(i);
                field.setAccessible(true);
                Object value = field.get(securityable);
                if (value == null) continue;
                if (value instanceof String _value) {
                    SecurityDecrypt decrypt = () -> {
                        try {
                            field.set(securityable, securityContext.getSecurityProcessor().decrypt(securityContext.getMethod(), _value));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    };
                    securityContext.addDecrypt(decrypt);
                } else if (value instanceof Securityable _value) {
                    buildSecurityableResult(securityContext, _value);
                } else {
                    buildArrayAndCollectionsResult(securityContext, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void buildArrayAndCollectionsResult(SecurityResultContext securityContext, Object object) {
        if (securityContext.isProcessed(object)) return;
        if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            if (length == 0) return;
            securityContext.addProcessed(object);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(object, i);
                if (value == null) continue;
                if (value instanceof String _value) {
                    int finalI = i;
                    SecurityDecrypt decrypt = () -> Array.set(object, finalI, securityContext.getSecurityProcessor().decrypt(securityContext.getMethod(), _value));
                    securityContext.addDecrypt(decrypt);
                } else if (value instanceof Securityable _value) {
                    buildSecurityableResult(securityContext, _value);
                } else {
                    buildArrayAndCollectionsResult(securityContext, value);
                }
            }
        } else if (object instanceof List || object instanceof Set) {
            Collection collection = (Collection) object;
            if (collection.isEmpty() || isImmutable(object.getClass())) return;
            securityContext.addProcessed(object);
            ArrayList<Object> origins = new ArrayList<Object>(collection);
            SecurityDecrypt decrypt = () -> {
                collection.clear();
                for (int i = 0; i < origins.size(); i++) {
                    Object value = origins.get(i);
                    if (value instanceof String _value) {
                        collection.add(securityContext.getSecurityProcessor().decrypt(securityContext.getMethod(), _value));
                    } else if (value instanceof Securityable _value) {
                        collection.add(_value);
                        buildSecurityableResult(securityContext, _value);
                    } else {
                        collection.add(value);
                        buildArrayAndCollectionsResult(securityContext, value);
                    }
                }
            };
            securityContext.addDecrypt(decrypt);
        } else if (object instanceof Map map) {
            if (map.isEmpty() || isImmutable(object.getClass())) return;
            String[] encryptMapKeys = securityContext.getSecurityWatch().encryptMapKeys();
            securityContext.addProcessed(object);
            Map<String, Object> origins = new LinkedHashMap<>(map);
            Set<String> encryptMapKeysSet = encryptMapKeys == null || encryptMapKeys.length == 0 ? map.keySet() : new HashSet<>(Arrays.asList(encryptMapKeys));
            SecurityDecrypt decrypt = () -> {
                map.clear();
                for (Map.Entry<String, Object> entry : origins.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String _value) {
                        if (encryptMapKeysSet.contains(key)) {
                            map.put(key, securityContext.getSecurityProcessor().decrypt(securityContext.getMethod(), _value));
                        } else {
                            map.put(key, _value);
                        }
                    } else if (value instanceof Securityable _value) {
                        map.put(key, _value);
                        buildSecurityableResult(securityContext, _value);
                    } else {
                        map.put(key, value);
                        if (encryptMapKeysSet.contains(key)) {
                            buildArrayAndCollectionsResult(securityContext, value);
                        }
                    }
                }
            };
            securityContext.addDecrypt(decrypt);
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

    static class SecurityParamsContext {
        private Method method;
        private SecurityWatch securityWatch;
        private SecurityProcessor securityProcessor;
        // 已build预处理的引用对象
        private Set<Object> processedSet = new HashSet<>();
        private List<SecurityEncrypt> encrypts = new ArrayList<>();
        private List<SecurityRecovery> recoveries = new ArrayList<>();
        // 已加密的数据Map（key=original, value=encrypted）
        private Map<String, String> encryptedMap = new HashMap<>();

        public SecurityParamsContext(Method method, SecurityWatch securityWatch, SecurityProcessor securityProcessor) {
            this.method = method;
            this.securityWatch = securityWatch;
            this.securityProcessor = securityProcessor;
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

        public void addEncrypt(SecurityEncrypt encrypt) {
            encrypts.add(encrypt);
        }

        public void add(SecurityEncrypt setter, SecurityRecovery recovery) {
            addEncrypt(setter);
            addRecovery(recovery);
        }

        public void addRecovery(SecurityRecovery recovery) {
            recoveries.add(recovery);
        }

        public List<SecurityEncrypt> getEncrypts() {
            return encrypts;
        }

        public List<SecurityRecovery> getRecoveries() {
            return recoveries;
        }

        public Map<String, String> getEncryptedMap() {
            return encryptedMap;
        }
    }

    static class SecurityResultContext {
        private Method method;
        private SecurityWatch securityWatch;
        private SecurityProcessor securityProcessor;
        private SecurityParamsContext securityParamsContext;
        private Set<Object> processedSet = new HashSet<>();
        private List<SecurityDecrypt> decrypts = new ArrayList<>();

        public SecurityResultContext(Method method, SecurityWatch securityWatch, SecurityProcessor securityProcessor, SecurityParamsContext securityParamsContext) {
            this.method = method;
            this.securityWatch = securityWatch;
            this.securityProcessor = securityProcessor;
            this.securityParamsContext = securityParamsContext;
            if (securityParamsContext != null) {
                processedSet.addAll(securityParamsContext.getProcessedSet());
            }
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

        public SecurityParamsContext getSecurityParamsContext() {
            return securityParamsContext;
        }

        public void addDecrypt(SecurityDecrypt setter) {
            decrypts.add(setter);
        }

        public List<SecurityDecrypt> getDecrypts() {
            return decrypts;
        }
    }
}
