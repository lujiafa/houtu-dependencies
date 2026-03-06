package io.github.lujiafa.houtu.core.exception;

import org.springframework.util.StringUtils;

/**
 * @ClassName ErrorMessageComposer
 * @date 2019年6月27日
 * @Description 错误码格式化工具
 */
public enum ErrorMessageComposer {

    PAREN_COLON("%s(%d)", "%s(%s:%d)"),
    PAREN_SPACED_COLON("%s (%d)", "%s (%s:%d)"),
    PAREN_DASH("%s(%d)", "%s(%s-%d)"),
    PAREN_SPACED_DASH("%s (%d)", "%s (%s-%d)"),
    BRACKET_DASH("%s[%d]", "%s[%s-%d]"),
    BRACKET_SPACED_DASH("%s [%d]", "%s [%s-%d]"),
    BRACKET_COLON("%s[%d]", "%s[%s:%d]"),
    BRACKET_SPACED_COLON("%s [%d]", "%s [%s:%d]");


    private final String pattern;
    private final String businessPattern;

    private ErrorMessageComposer(String pattern, String businessPattern) {
        this.pattern = pattern;
        this.businessPattern = businessPattern;
    }

    public ErrorCode format(ErrorCode errorCode, String businessCode) {
        if (StringUtils.hasLength(businessCode)) {
            return new ErrorCode(errorCode.getCode(), String.format(businessPattern, errorCode.getMessage(), businessCode, errorCode.getCode()));
        }
        return new ErrorCode(errorCode.getCode(), String.format(pattern, errorCode.getMessage(), errorCode.getCode()));
    }
}
