package io.github.lujiafa.houtu.web.view;

import io.github.lujiafa.houtu.core.context.SpringApplicationContext;
import io.github.lujiafa.houtu.web.handler.ExtensionHandlerMethodReturnValueHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.servlet.View;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SmartView implements View {

    protected static Logger logger = LoggerFactory.getLogger(SmartView.class);

    protected Object data;
    protected Charset charset = StandardCharsets.UTF_8;

    protected static volatile ExtensionHandlerMethodReturnValueHandler returnValueHandler;

    public SmartView(Object data) {
        this.data = data;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (data == null) return;
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        getReturnValueHandler().write(data, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));
    }

    private ExtensionHandlerMethodReturnValueHandler getReturnValueHandler() {
        if (returnValueHandler != null) {
            return returnValueHandler;
        }
        synchronized (SmartView.class) {
            if (returnValueHandler != null) {
                return returnValueHandler;
            }
            returnValueHandler = SpringApplicationContext.getBean(ExtensionHandlerMethodReturnValueHandler.class);
        }
        return returnValueHandler;
    }

}