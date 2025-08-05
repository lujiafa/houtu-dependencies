package com.houtu.springcloud.feign.provider;

import com.houtu.springcloud.feign.constant.FeignConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * feign provider端校验请求拦截器
 * @author jonlu
 * @date 2022/9/23
 */
public class FeignSecurityHandlerInterceptor implements HandlerInterceptor {

    private String secret;

    public FeignSecurityHandlerInterceptor(String secret) {
        this.secret = secret;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (secret == null || secret.isEmpty()) return true;
        String secretCode = request.getHeader(FeignConstant.FEIGN_REQUEST_SECRET_CODE);
        if (secretCode == null || secretCode.isEmpty()) return false;
        return Objects.equals(secret, secretCode);
    }
}
