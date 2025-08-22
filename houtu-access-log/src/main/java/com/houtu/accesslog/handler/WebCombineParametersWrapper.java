package com.houtu.accesslog.handler;

import com.houtu.web.util.WebCombineParametersSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * @Description: 支持桥接获取CombineModelMap
 * @Author: jonlu
 * @Date: 2018/9/5
 */
public class WebCombineParametersWrapper {

    public Map getBodyParameterMap(HttpServletRequest request, HttpServletResponse response) {
        return WebCombineParametersSupport.getBodyParameterMap(request, response);
    }
}
