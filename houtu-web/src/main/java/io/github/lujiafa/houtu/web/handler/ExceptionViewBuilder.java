package io.github.lujiafa.houtu.web.handler;

import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.web.view.SmartErrorView;
import org.springframework.web.servlet.View;

/**
 * @date 2025年6月4日
 * @Description 异常视图构建器
 */
public class ExceptionViewBuilder {

    public View build(ErrorCode errorCode) {
        return new SmartErrorView(errorCode);
    }
}
