package com.houtu.monitor.util;

import com.houtu.monitor.annotation.ReqMonitor;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.annotation.Annotation;

/**
 * @author jon
 * @date 2020年12月17日
 */
public class WebMonitorUtils {

    /**
     * 获取默认请求的Path注解
     * @param request 请求
     * @return ReqMonitor
     */
    public static ReqMonitor getRequestMonitorAnnotation(HttpServletRequest request) {
        return new ReqMonitor() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ReqMonitor.class;
            }

            @Override
            public String cmd() {
                String requestURI = request.getRequestURI();
                if (requestURI.contains("{"))
                    return requestURI.replaceAll("\\{[^/]+?\\}", "{}");
                return requestURI;
            }
        };
    }
}
