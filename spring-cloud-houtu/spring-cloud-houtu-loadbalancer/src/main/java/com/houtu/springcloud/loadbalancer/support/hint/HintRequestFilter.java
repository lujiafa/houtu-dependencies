package com.houtu.springcloud.loadbalancer.support.hint;

import com.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class HintRequestFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            HintContext.set(httpServletRequest.getHeader(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME));
        }
        filterChain.doFilter(request, response);
        HintContext.remove();
    }

}
