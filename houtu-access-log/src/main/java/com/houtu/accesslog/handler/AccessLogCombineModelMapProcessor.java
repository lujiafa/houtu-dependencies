package com.houtu.accesslog.handler;

import com.houtu.web.util.WebCombineModelMapSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * @Description: 支持桥接获取CombineModelMap
 * @Author: jonlu
 * @Date: 2018/9/5
 */
public class AccessLogCombineModelMapProcessor {

    public Map getCombineModelMap(HttpServletRequest request, HttpServletResponse response) {
        return WebCombineModelMapSupport.getCombineModelMap(request, response);
    }
}
