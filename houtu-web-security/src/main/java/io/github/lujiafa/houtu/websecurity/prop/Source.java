package io.github.lujiafa.houtu.websecurity.prop;

/**
 * 签名数据来源
 */
public enum Source {

    /** 仅从请求头取 */
    HEADER,

    /** 仅从请求参数（queryString + body）取 */
    BODY,

    /** 两者都取：先取请求头，取不到再取请求参数 */
    BOTH;
}
