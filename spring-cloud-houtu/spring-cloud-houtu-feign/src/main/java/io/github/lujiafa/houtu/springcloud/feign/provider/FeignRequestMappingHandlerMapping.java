package io.github.lujiafa.houtu.springcloud.feign.provider;

import io.github.lujiafa.houtu.springcloud.feign.anotation.AutoFeign;
import io.github.lujiafa.houtu.springcloud.feign.constant.FeignConstant;
import io.github.lujiafa.houtu.util.common.AnnotationUtils;
import io.github.lujiafa.houtu.util.constant.CharConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * @date 2019年7月13日
 * @author jonlu
 */
public class FeignRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	@Override
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		HandlerMethod handlerMethod = super.getHandlerInternal(request);
		HandlerMethod  actualHandlerMethod = null;
		if (handlerMethod != null &&
				((actualHandlerMethod = handlerMethod.getResolvedFromHandlerMethod()) instanceof InternalHandlerMethod
					|| (actualHandlerMethod = handlerMethod) instanceof InternalHandlerMethod)) {
			request.setAttribute(FeignConstant.FEIGN_PROVIDER_AUTO_HANDLER_ATTR_NAME, ((InternalHandlerMethod) actualHandlerMethod).getAutoFeign());
		}
		return handlerMethod;
	}

	@Override
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		return super.getHandlerExecutionChain(handler, request);
	}

	@Override
	protected boolean isHandler(Class<?> beanType) {
		Class<?> userType = ClassUtils.getUserClass(beanType);
		if (Proxy.class.isAssignableFrom(userType) || ClassUtils.isCglibProxyClass(userType)) {
			return false;
		}
		Class<?> interfaceClass = getInterface(userType, false);
		if (interfaceClass != null
				&& AnnotatedElementUtils.hasAnnotation(interfaceClass, FeignClient.class)) {
			Method[] methods = beanType.getMethods();
			if (methods == null || methods.length == 0) {
				return false;
			}
			return Arrays.stream(methods).parallel().anyMatch(m -> {
				AutoFeign autoFeign = AnnotationUtils.getAnnotationByPriorityMethod(m, AutoFeign.class);
				return autoFeign != null && autoFeign.value();
			});
		}
		return false;
	}

	@Override
	protected void detectHandlerMethods(Object handler) {
		Object actualHandler = (handler instanceof String ? obtainApplicationContext().getBean((String) handler) : handler);
		if (actualHandler == null) {
			return;
		}
		Class<?> userType = ClassUtils.getUserClass(actualHandler.getClass());
		Class<?> interfaceClass = getInterface(userType, true);
		// TargetSource targetSource = new SingletonTargetSource(metrics);
		// Object actualHandler = ProxyFactory.getProxy(interfaceClass, targetSource);
		// Class<?> handlerType = actualHandler.getClass();

		Map<Method, AutoFeign> methodAutoFeignMap = new HashMap<>();
		Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
				(MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
					try {
						AutoFeign autoFeign = null;
						Method interfaceMethod = null;
						if ((interfaceMethod = ReflectionUtils.findMethod(interfaceClass, method.getName(), method.getParameterTypes())) == null
								|| AnnotationUtils.findAnnotation(interfaceMethod, RequestMapping.class) == null
								|| (autoFeign = AnnotationUtils.getAnnotationByPriorityMethod(method, AutoFeign.class)) == null
								|| !autoFeign.value()) {
							// 丢弃:
							//   1.Method并非来自@FeignClient接口
							//   2.@FeignClient接口Method上未添加@RequestMapping注解
							//   3.未在方法、父方法、类、父类或接口就近匹配到@AutoFeign
							//   4.@AutoFeign明确已禁用
							return null;
						}
						RequestMappingInfo requestMappingInfo = getMappingForMethod(method, interfaceClass);
						if (requestMappingInfo != null) {
							methodAutoFeignMap.put(method, autoFeign);
						}
						return requestMappingInfo;
					}
					catch (Throwable ex) {
						throw new IllegalStateException("Invalid mapping on metrics class [" +
								interfaceClass.getName() + "]: " + method, ex);
					}
				});
		methods.forEach((method, mapping) -> {
			Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
			registerHandlerMethod(new InternalHandlerWrapper(actualHandler, methodAutoFeignMap.get(method)), invocableMethod, mapping);
		});
	}

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = createRequestMappingInfo(method);
		if (info != null) {
			RequestMappingInfo typeInfo = createClassRequestMappingInfo(handlerType);
			if (typeInfo != null) {
				info = typeInfo.combine(info);
			}
		}
		return info;
	}

	@Override
	protected HandlerMethod createHandlerMethod(Object handler, Method method) {
		InternalHandlerWrapper internalHandlerWrapper = ((InternalHandlerWrapper) handler);
		AutoFeign autoFeign = internalHandlerWrapper.getAutoFeign();
		handler = internalHandlerWrapper.getHandler();
		return handler instanceof String ? new InternalHandlerMethod((String)handler, this.obtainApplicationContext().getAutowireCapableBeanFactory(), this.obtainApplicationContext(), method, autoFeign) : new InternalHandlerMethod(handler, method, autoFeign);
	}

	private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
		RequestMapping requestMapping = (RequestMapping)AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
		RequestCondition<?> condition = element instanceof Class ? this.getCustomTypeCondition((Class)element) : this.getCustomMethodCondition((Method)element);
		return requestMapping != null ? this.createRequestMappingInfo(requestMapping, condition) : null;
	}
	
	private RequestMappingInfo createClassRequestMappingInfo(Class<?> element) {
		FeignClient feignClient = AnnotatedElementUtils.findMergedAnnotation(element, FeignClient.class);
		RequestMapping requestMapping = null;
		if (feignClient != null
				&& StringUtils.hasText(feignClient.path())
				&& !"/".equals(feignClient.path())) {
			requestMapping = new RequestMapping() {
				@Override
				public Class<? extends Annotation> annotationType() {return RequestMapping.class;}
				@Override
				public String[] value() {return new String[] {feignClient.path()};}
				@Override
				public String[] produces() {return new String[0];}
				@Override
				public String[] path() {return new String[]{feignClient.path()};}
				@Override
				public String[] params() {return new String[0];}
				@Override
				public String name() {return CharConstant.EMPTY;}
				@Override
				public RequestMethod[] method() {return new RequestMethod[0];}
				@Override
				public String[] headers() {return new String[0];}
				@Override
				public String[] consumes() {return new String[0];}
				@Override
				public String version() {return CharConstant.EMPTY;}
			};
		}
		RequestCondition<?> condition = getCustomTypeCondition((Class<?>) element);
		return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
	}
	
	private Class<?> getInterface(Class<?> cls, boolean nullToException) {
		for (Class<?> clazz : cls.getInterfaces()) {
			if (clazz.getAnnotation(FeignClient.class) != null) {
				return clazz;
			}
		}
		if (nullToException) {
			throw new IllegalArgumentException("get @FeignClient fail");
		}
		return null;
	}

	static class InternalHandlerWrapper {
		private Object handler;
		private AutoFeign autoFeign;

		public InternalHandlerWrapper(Object handler, AutoFeign autoFeign) {
			this.handler = handler;
			this.autoFeign = autoFeign;
		}

		public Object getHandler() {
			return handler;
		}

		public AutoFeign getAutoFeign() {
			return autoFeign;
		}
	}

	static class InternalHandlerMethod extends HandlerMethod {
		private AutoFeign autoFeign;

		public InternalHandlerMethod(Object bean, Method method, AutoFeign autoFeign) {
			super(bean, method);
			this.autoFeign = autoFeign;
		}

		public InternalHandlerMethod(String beanName, BeanFactory beanFactory, MessageSource messageSource, Method method, AutoFeign autoFeign) {
			super(beanName, beanFactory, messageSource, method);
			this.autoFeign = autoFeign;
		}

		public AutoFeign getAutoFeign() {
			return autoFeign;
		}
	}

}