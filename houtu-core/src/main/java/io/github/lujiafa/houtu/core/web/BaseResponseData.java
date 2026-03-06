package io.github.lujiafa.houtu.core.web;

public interface BaseResponseData<T> {

    int getCode();

    String getMessage();

    T getData();
}
