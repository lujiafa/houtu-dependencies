package com.houtu.util.web;

import com.houtu.util.common.JsonUtils;
import com.houtu.util.common.XmlUtils;
import com.houtu.util.constant.CharConstant;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WebUtils extends org.springframework.web.util.WebUtils {

    private static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

    private final static String ATTRIBUTE_NAME_REQUEST_MEDIA_TYPE = "::REQUEST_MEDIA_TYPE::";
    private final static String ATTRIBUTE_NAME_RESPONSE_MEDIA_TYPE = "::RESPONSE_MEDIA_TYPE::";
    private final static String IP_UNKNOWN = "unknown";
    private final static String IP_LOCAL = "127.0.0.1";

    /**
     * @Title getRequest
     * @Description 应用中获取request对象
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return getServletRequestAttributes().getRequest();
    }

    /**
     * @Title getResponse
     * @Description 应用中获取response对象
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }

    /**
     * @Description 获取ServletRequestAttributes对象
     * @Title getServletRequestAttributes
     * @return ServletRequestAttributes
     */
    public static ServletRequestAttributes getServletRequestAttributes() {
        RequestAttributes reqAttr = RequestContextHolder.getRequestAttributes();
        if (reqAttr != null && reqAttr instanceof ServletRequestAttributes) {
            return (ServletRequestAttributes) reqAttr;
        }
        throw new RuntimeException("ServletRequestAttributes fetch failed, please check the configuration is correct.");
    }

    /**
     * @param request
     * @return boolean true-是 false-否
     * @Title isHttpPost
     * @Description 判断请求是否为post请求
     */
    public static boolean isHttpPost(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return RequestMethod.POST.equals(getRequestMethod(request));
    }

    /**
     * @param request
     * @return boolean true-是 false-否
     * @Title isHttpGet
     * @Description 判断请求是否为get请求
     */
    public static boolean isHttpGet(HttpServletRequest request) {
        return RequestMethod.GET.equals(getRequestMethod(request));
    }

    /**
     * @param request
     * @return boolean true-是 false-否
     * @Title isHttpMultipart
     * @Description 是否为Multipart请求
     */
    public static boolean isHttpMultipart(HttpServletRequest request) {
        if (isHttpGet(request)) {
            return false;
        }
        MediaType mediaType = WebUtils.getRequestMediaType(request);
        if (MediaType.MULTIPART_FORM_DATA.includes(mediaType)) {
            return true;
        }
        return false;
    }

    /**
     * @param request
     * @return RequestMethod
     * @Title getRequestMethod
     * @Description 获取请求方法类型
     */
    public static RequestMethod getRequestMethod(HttpServletRequest request) {
        return RequestMethod.valueOf(request.getMethod());
    }

    /**
     * @Title getUrlEncodedParams 获取urlencoded参数集合
     * @param request 请求对象
     * @return Map<String, String> urlencoded参数集合
     * @throws IOException
     */
    public static Map<String, String> getUrlEncodedParams(HttpServletRequest request) throws IOException {
        Map<String, String> cacheMap = new LinkedHashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            cacheMap.put(name, request.getParameter(name));
        }
        return cacheMap;
    }

    public static byte[] getRequestBodyStream(HttpServletRequest request) throws IOException {
        if (isHttpGet(request)) {
            throw new RuntimeException("getRequestBodyParams failed, please check the request method.");
        }
        return StreamUtils.copyToByteArray(request.getInputStream());
    }

    /**
     * @param request 请求对象
     * @param name    请求头中session对应Key名
     * @return Session ID
     * @Title getSessionId
     * @Description 获取Session ID
     */
    public static String getSessionId(HttpServletRequest request, String name) {
        Assert.hasText(name, "parameter session name is empty");
        String sessionId = request.getHeader(name);
        if (sessionId != null
                && sessionId.length() > 0) {
            return sessionId;
        }
        Map<String, String> cookieMap = getCookieMap(request);
        return cookieMap.get(name);
    }

    /**
     * @param request 请求对象
     * @return cookie信息
     * @Title getCookieMap
     * @Description 获取cookie信息
     */
    public static Map<String, String> getCookieMap(HttpServletRequest request) {
        Map<String, String> cookieMap = new HashMap<String, String>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null
                && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        }
        return cookieMap;
    }

    /**
     * @param response 响应对象
     * @param name     cookie名称
     * @param value    cookie值
     * @Title writeCookie
     * @Description 写出cookie
     */
    public static boolean writeCookie(HttpServletResponse response, String name, String value) {
        return writeCookie(response, name, value, null, null, null);
    }

    /**
     * @param response 响应对象
     * @param name     cookie名称
     * @param value    cookie值
     * @param expire   有效期（秒）
     * @Title writeCookie
     * @Description 写出cookie
     */
    public static boolean writeCookie(HttpServletResponse response, String name, String value, Integer expire) {
        return writeCookie(response, name, value, null, null, expire);
    }

    /**
     * @param response 响应对象
     * @param name     cookie名称
     * @param value    cookie值
     * @param path     路径
     * @param domain   域名
     * @Title writeCookie
     * @Description 写出cookie
     */
    public static boolean writeCookie(HttpServletResponse response, String name, String value, String path, String domain) {
        return writeCookie(response, name, value, path, domain, null);
    }

    /**
     * @param response 响应对象
     * @param name     cookie名称
     * @param value    cookie值
     * @param path     路径
     * @param domain   域名
     * @param expire   有效期（秒）
     * @Title writeCookie
     * @Description 写出cookie
     */
    public static boolean writeCookie(HttpServletResponse response, String name, String value, String path, String domain, Integer expire) {
        Assert.notNull(response, "object response cannot be null");
        Assert.hasText(name, "cookie name can not be empty");
        Cookie cookie = new Cookie(name, value);
        if (path == null) {
            cookie.setPath("/");
        } else {
            cookie.setPath(path);
        }
        if (StringUtils.hasLength(domain)) {
            cookie.setDomain(domain);
        }
        if (expire == null) {
            cookie.setMaxAge(-1);
        } else {
            cookie.setMaxAge(expire);
        }
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return true;
    }

    /**
     * @param request  请求对象
     * @param response 响应对象
     * @param name     cookie名
     * @Title removeCookie
     * @Description 移除请求头中cookie
     */
    public static boolean removeCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Assert.notNull(response, "object response cannot be null");
        Assert.hasText(name, "cookie name can not be empty");
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie != null
                        && name.equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        return true;
    }

    /**
     * @param request
     * @return MediaType
     * @Title getRequestMediaType
     * @Description 获取客户端请求MIME类型
     */
    public static MediaType getRequestMediaType(ServletRequest request) {
        MediaType mediaType = (MediaType) request.getAttribute(ATTRIBUTE_NAME_REQUEST_MEDIA_TYPE);
        if (mediaType == null) {
            mediaType = getRequestMediaTypes(request).get(0);
            request.setAttribute(ATTRIBUTE_NAME_REQUEST_MEDIA_TYPE, mediaType);
        }
        return mediaType;
    }

    /**
     * @param request
     * @return MediaType
     * @Title getRequestMediaTypes
     * @Description 获取客户端请求MIME类型
     */
    public static List<MediaType> getRequestMediaTypes(ServletRequest request) {
        String contentType = request.getContentType();
        if (StringUtils.hasLength(contentType)) {
            try {
                List<MediaType> mediaTypes = MediaType.parseMediaTypes(contentType);
                if (mediaTypes != null && mediaTypes.size() > 0) {
                    return mediaTypes;
                }
            } catch (Exception e) {
                logger.debug("获取请求头accept参数失败|{}", e.getMessage());
            }
        }
        return Arrays.asList(MediaType.ALL);
    }

    /**
     * @param request
     * @return MediaType
     * @Title getResponseMediaType
     * @Description 获取客户端需要的响应Media类型
     */
    public static MediaType getResponseMediaType(HttpServletRequest request) {
        MediaType mediaType = (MediaType) request.getAttribute(ATTRIBUTE_NAME_RESPONSE_MEDIA_TYPE);
        if (mediaType == null) {
            mediaType = getResponseMediaTypes(request).get(0);
            request.setAttribute(ATTRIBUTE_NAME_RESPONSE_MEDIA_TYPE, mediaType);
        }
        return mediaType;
    }

    /**
     * @param request
     * @return MediaType
     * @Title getResponseMediaTypes
     * @Description 获取客户端需要的响应Media类型
     */
    public static List<MediaType> getResponseMediaTypes(HttpServletRequest request) {
        String headerAccept = request.getHeader(HttpHeaders.ACCEPT);
        if (StringUtils.hasLength(headerAccept)) {
            try {
                List<MediaType> mediaTypes = MediaType.parseMediaTypes(headerAccept);
                if (mediaTypes != null && mediaTypes.size() > 0) {
                    MimeTypeUtils.sortBySpecificity(mediaTypes);
                    return mediaTypes;
                }
            } catch (Exception e) {
                logger.debug("获取请求头accept参数失败|{}", e.getMessage());
            }
        }
        return Arrays.asList(MediaType.ALL);
    }

    /**
     * @param request
     * @return String
     * @Title getRequestIp
     * @Description 获取请求IP
     */
    public static String getRequestIp(HttpServletRequest request) {
        String requestIp = getFirstIp(request.getHeader("x-forwarded-for"));
        if (requestIp == null || requestIp.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(requestIp)) {
            requestIp = getFirstIp(request.getHeader("Proxy-Client-IP"));
        }
        if (requestIp == null || requestIp.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(requestIp)) {
            requestIp = getFirstIp(request.getHeader("WL-Proxy-Client-IP"));
        }
        if (requestIp == null || requestIp.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(requestIp)) {
            requestIp = getFirstIp(request.getHeader("HTTP_CLIENT_IP"));
        }
        if (requestIp == null || requestIp.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(requestIp)) {
            requestIp = getFirstIp(request.getHeader("HTTP_X_FORWARDED_FOR"));
        }
        if (requestIp == null || requestIp.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(requestIp)) {
            requestIp = getFirstIp(request.getHeader("X-Real-IP"));
        }
        if (requestIp == null || requestIp.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(requestIp)) {
            requestIp = getFirstIp(request.getRemoteAddr());
            if (requestIp != null
                    && (IP_LOCAL.equals(requestIp)
                    || "0:0:0:0:0:0:0:1".equals(requestIp))) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    String hostAddress = inetAddress.getHostAddress();
                    if (!StringUtils.hasLength(hostAddress)) {
                        requestIp = IP_LOCAL;
                    } else {
                        requestIp = getFirstIp(hostAddress);
                    }
                } catch (UnknownHostException e) {
                    requestIp = IP_LOCAL;
                }
            }
        }
        if (IP_UNKNOWN.equalsIgnoreCase(requestIp)) {
            return IP_LOCAL;
        }
        if (requestIp != null) {
            return requestIp.trim();
        }
        throw new RuntimeException("获取请求IP信息失败");
    }

    /**
     * @param request
     * @return String
     * @Title getRequestIpChain
     * @Description 获取请求IP链
     */
    public static String getRequestIpChain(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getHeader("x-forwarded-for")).append(CharConstant.VERTICAL_BAR)
                .append(request.getHeader("Proxy-Client-IP")).append(CharConstant.VERTICAL_BAR)
                .append(request.getHeader("WL-Proxy-Client-IP")).append(CharConstant.VERTICAL_BAR)
                .append(request.getHeader("HTTP_CLIENT_IP")).append(CharConstant.VERTICAL_BAR)
                .append(request.getHeader("HTTP_X_FORWARDED_FOR")).append(CharConstant.VERTICAL_BAR)
                .append(request.getHeader("X-Real-IP")).append(CharConstant.VERTICAL_BAR)
                .append(request.getRemoteAddr());
        return builder.toString();
    }


    /**
     * @param ipArrayStr
     * @return String
     * @Title getFirstIp
     * @Description 工具方法，用于获取ip英文逗号分割数组中第一个ip值
     */
    private static String getFirstIp(String ipArrayStr) {
        if (ipArrayStr == null
                || ipArrayStr.length() == 0) {
            return "";
        }
        String[] ipArray = ipArrayStr.split(CharConstant.COMMA);
        for (String ip : ipArray) {
            if (!StringUtils.hasLength(ip)
                    || "unknown".equalsIgnoreCase(ip)) {
                continue;
            }
            return ip;
        }
        return "";
    }

    /**
     * @Description 输出json
     * @param response 响应对象
     * @param value 待输出对象
     */
    public static void writeJson(HttpServletResponse response, Object value) {
        String json;
        if (value instanceof CharSequence) {
            json = value.toString();
        } else {
            json = JsonUtils.toStringIgnoreNull(value);
        }
        write(response, json);
    }

    /**
     * @Description 输出json
     * @param response 响应对象
     * @param value 待输出对象
     */
    public static void writeXml(HttpServletResponse response, Object value) {
        String json;
        if (value instanceof CharSequence) {
            json = value.toString();
        } else {
            json = XmlUtils.toXml(value, StandardCharsets.UTF_8);
        }
        write(response, json);
    }

    /**
     * @Description 输出json
     * @param response 响应对象
     * @param value 待输出对象
     */
    public static void write(HttpServletResponse response, String value) {
        Assert.notNull(response, "response cannot be null");
        Assert.notNull(value, "value cannot be null");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8.toString());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(value);
            writer.flush();
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}