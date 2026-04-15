package io.github.lujiafa.houtu.springcloud.sentinel.handler.webflux;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;


public class SimpleBlockRequestHandler implements BlockRequestHandler {

    private URI blockPage;

    public SimpleBlockRequestHandler() {
    }

    public SimpleBlockRequestHandler(URI blockPage) {
        this.blockPage = blockPage;
    }

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
        List<MediaType> acceptedMediaTypes = serverWebExchange.getRequest().getHeaders().getAccept();
        MediaType mediaType = MediaType.ALL;
        if (acceptedMediaTypes != null && acceptedMediaTypes.size() > 0) {
            acceptedMediaTypes.remove(MediaType.ALL);
            if (!acceptedMediaTypes.isEmpty()) {
                mediaType = acceptedMediaTypes.get(0);
            }
        }
        if (MediaType.APPLICATION_XML.includes(mediaType)
                || MediaType.TEXT_XML.includes(mediaType)
                || MediaType.APPLICATION_XHTML_XML.includes(mediaType)) {
            ErrorCode errorCode = ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, new Object[]{throwable.getMessage() == null ? "block" : throwable.getMessage()});
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(fromValue(errorCode));
        } else if (MediaType.TEXT_HTML.includes(mediaType)) {
            if (blockPage != null) {
                return ServerResponse.temporaryRedirect(blockPage).build();
            }
        }
        ErrorCode errorCode = ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, new Object[]{throwable.getMessage() == null ? "block" : throwable.getMessage()});
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(errorCode));
    }

}
