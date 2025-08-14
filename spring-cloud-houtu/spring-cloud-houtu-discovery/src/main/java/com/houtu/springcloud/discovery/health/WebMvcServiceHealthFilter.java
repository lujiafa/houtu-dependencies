package com.houtu.springcloud.discovery.health;

import com.houtu.springcloud.discovery.context.ServiceContext;
import com.houtu.springcloud.discovery.type.ServiceStatus;
import com.houtu.util.web.WebUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

public class WebMvcServiceHealthFilter implements Filter {

    private ServiceContext serviceContext;

    public WebMvcServiceHealthFilter(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletResponse instanceof HttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        if (serviceContext.getServiceState() == ServiceStatus.UP) {
            httpServletResponse.setStatus(200);
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            WebUtils.writeJson(httpServletResponse, "{\"status\":\"UP\"}");
        } else {
            httpServletResponse.setStatus(503);
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            WebUtils.writeJson(httpServletResponse, "{\"status\":\"DOWN\"}");
        }
    }
}
