package io.github.lujiafa.houtu.core.exception;

import org.springframework.core.Ordered;

/**
 * 错误信息国际化配置提供接口
 */
public interface ErrorI18nProvider extends Ordered {

    /**
     * 错误码本地化资源文件
     * @return 本地化资源文件基础名
     */
    String getBasename();

}
