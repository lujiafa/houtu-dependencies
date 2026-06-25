package io.github.lujiafa.houtu.core.web;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;

public interface BaseResponseData<T> {

    default boolean hasSuccess() {
        return ErrorCodeConstant.SUCCESS.equals(getCode());
    }

    int getCode();

    String getMessage();

    T getData();
}
