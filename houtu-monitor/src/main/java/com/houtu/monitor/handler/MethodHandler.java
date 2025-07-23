package com.houtu.monitor.handler;

@FunctionalInterface
public interface MethodHandler {

    Object proceed() throws Throwable;
}
