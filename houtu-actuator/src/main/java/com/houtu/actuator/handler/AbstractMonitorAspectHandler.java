//package com.houtu.actuator.handler;
//
//import com.houtu.core.constant.ErrorCodeConstant;
//import com.houtu.core.exception.BusinessException;
//import com.houtu.core.web.BaseResponseData;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Method;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @author jon
// * @date 2020年12月23日
// */
//public abstract class AbstractMonitorAspectHandler<T extends Annotation> {
//
//	protected Logger logger = LoggerFactory.getLogger(getClass());
//
//	protected Object process(MethodHandler handler, Method method) throws Throwable {
//		T monitor = getAnnotation(method);
//		long start = System.currentTimeMillis();
//		Integer code = null;
//		try {
//			Object object = handler.proceed();
//			if (object instanceof BaseResponseData) {
//				code = ((BaseResponseData) object).getCode();
//			} else {
//				code = ErrorCodeConstant.SUCCESS;
//			}
//			return object;
//		} catch (Throwable e) {
//			if (e instanceof BusinessException) {
//				code = ((BusinessException) e).getErrorCode().getCode();
//			} else {
//				Throwable ex = e;
//				Set<Throwable> nestedExceptions = new HashSet<>();
//				while (ex != null && !nestedExceptions.contains(ex)) {
//					if (ex instanceof BusinessException) {
//						code = ((BusinessException) ex).getErrorCode().getCode();
//						break;
//					}
//					nestedExceptions.add(ex);
//					ex = ex.getCause();
//				}
//				if (code == null) {
//					code = ErrorCodeConstant.SERVER_BUSY;
//				}
//			}
//			throw e;
//		} finally {
//			try {
//				monitorLog(monitor, code, System.currentTimeMillis() - start);
//			}  catch (Throwable e) {
//				if (logger.isDebugEnabled()) {
//					logger.debug("monitorLog error", e);
//				}
//			}
//		}
//	}
//
//	protected abstract T getAnnotation(Method method);
//
//    /**
//     *  监控日志记录
//     */
//    protected abstract void monitorLog(T annotation, int code, long cost);
//
//}
//package com.houtu.actuator.handler;
//
//import com.houtu.core.constant.ErrorCodeConstant;
//import com.houtu.core.exception.BusinessException;
//import com.houtu.core.web.BaseResponseData;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Method;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @author jon
// * @date 2020年12月23日
// */
//public abstract class AbstractMonitorAspectHandler<T extends Annotation> {
//
//	protected Logger logger = LoggerFactory.getLogger(getClass());
//
//	protected Object process(MethodHandler handler, Method method) throws Throwable {
//		T monitor = getAnnotation(method);
//		long start = System.currentTimeMillis();
//		Integer code = null;
//		try {
//			Object object = handler.proceed();
//			if (object instanceof BaseResponseData) {
//				code = ((BaseResponseData) object).getCode();
//			} else {
//				code = ErrorCodeConstant.SUCCESS;
//			}
//			return object;
//		} catch (Throwable e) {
//			if (e instanceof BusinessException) {
//				code = ((BusinessException) e).getErrorCode().getCode();
//			} else {
//				Throwable ex = e;
//				Set<Throwable> nestedExceptions = new HashSet<>();
//				while (ex != null && !nestedExceptions.contains(ex)) {
//					if (ex instanceof BusinessException) {
//						code = ((BusinessException) ex).getErrorCode().getCode();
//						break;
//					}
//					nestedExceptions.add(ex);
//					ex = ex.getCause();
//				}
//				if (code == null) {
//					code = ErrorCodeConstant.SERVER_BUSY;
//				}
//			}
//			throw e;
//		} finally {
//			try {
//				monitorLog(monitor, code, System.currentTimeMillis() - start);
//			}  catch (Throwable e) {
//				if (logger.isDebugEnabled()) {
//					logger.debug("monitorLog error", e);
//				}
//			}
//		}
//	}
//
//	protected abstract T getAnnotation(Method method);
//
//    /**
//     *  监控日志记录
//     */
//    protected abstract void monitorLog(T annotation, int code, long cost);
//
//}
