package io.github.lujiafa.houtu.springcloud.sentinel.handler.webmvc;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.util.web.WebUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleBlockExceptionHandler implements BlockExceptionHandler {

    private String blockPage;

    public SimpleBlockExceptionHandler() {}

    public SimpleBlockExceptionHandler(String blockPage) {
        this.blockPage = blockPage;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        MediaType mediaType = WebUtils.getResponseMediaType(request);
        ErrorCode errorCode = ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, new Object[]{e.getMessage() == null ? "block" : e.getMessage()});
        if (MediaType.APPLICATION_XML.includes(mediaType)
                || MediaType.TEXT_XML.includes(mediaType)
                || MediaType.APPLICATION_XHTML_XML.includes(mediaType)) {
            WebUtils.writeXml(response, errorCode);
            return;
        } else if (MediaType.TEXT_HTML.includes(mediaType)) {
            if (StringUtils.hasLength(blockPage)) {
                response.sendRedirect(blockPage);
                return;
            }
        }
        WebUtils.writeJson(response, errorCode);
    }
}
