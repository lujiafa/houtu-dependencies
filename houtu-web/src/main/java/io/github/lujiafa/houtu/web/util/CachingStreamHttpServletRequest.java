package io.github.lujiafa.houtu.web.util;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

/**
 * 可重复读取的HttpServletRequest（参考：ContentCachingRequestWrapper）
 */
public class CachingStreamHttpServletRequest extends HttpServletRequestWrapper implements AutoCloseable {
    static final Logger logger = LoggerFactory.getLogger(CachingStreamHttpServletRequest.class);

    public CachingStreamHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return super.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return super.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return super.getParameterNames();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        String characterEncoding = getCharacterEncoding();
        if (StringUtils.hasLength(characterEncoding)) {
            charset = Charset.forName(characterEncoding);
        }
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }

    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return (enc != null ? enc : StandardCharsets.UTF_8.name());
    }

    private ByteArrayOutputStream cachingOutputStream;

    public void releaseCache() {
        cachingOutputStream = null;
    }

    @Override
    public void close() {
        releaseCache();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayOutputStream cachedInputStream = loadStream();
        InputStream inputStream = new ByteArrayInputStream(cachedInputStream.toByteArray());
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public void close() throws IOException {
                super.close();
            }
        };
    }

    private synchronized ByteArrayOutputStream loadStream() {
        if (cachingOutputStream != null) {
            return cachingOutputStream;
        }
        try {
            ServletInputStream servletInputStream = super.getInputStream();
            servletInputStream.transferTo(cachingOutputStream = new ByteArrayOutputStream());
            servletInputStream.close();
        } catch (Exception e) {
            logger.error("获取请求数据异常|{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.build(ErrorCodeConstant.DATA_LOADING_FAILED, getRequest().getLocale()), e);
        }
        return cachingOutputStream;
    }
}