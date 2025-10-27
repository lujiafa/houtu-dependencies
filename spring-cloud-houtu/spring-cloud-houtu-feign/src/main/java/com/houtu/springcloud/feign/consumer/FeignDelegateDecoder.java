package com.houtu.springcloud.feign.consumer;

import com.houtu.core.exception.ErrorCode;
import com.houtu.core.web.BaseResponseData;
import com.houtu.springcloud.feign.provider.FeignThroughBusinessException;
import com.houtu.springcloud.feign.util.ExceptionHeader;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 解码器，使用详见BaseBuilder
 *
 * @author: jonlu
 * @date: 2018/7/27
 */
public class FeignDelegateDecoder implements Decoder {

    private final Decoder delegate;

    public FeignDelegateDecoder(Decoder delegate) {
        Objects.requireNonNull(delegate, "Decoder must not be null. ");
        this.delegate = delegate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        checkResponseException(response, type);
        if (!isOptional(type)) {
            return this.delegate.decode(response, type);
        } else if (response.status() != 404 && response.status() != 204) {
            Type enclosedType = Util.resolveLastTypeParameter(type, Optional.class);
            return Optional.ofNullable(this.delegate.decode(response, enclosedType));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Decoder处理场景为HTTP状态码2xx时，无需考虑404和204，404和204场景下，通过Header传递异常信息，由FeignDelegateDecoder处理
     *
     * @param response
     * @throws IOException
     */
    void checkResponseException(Response response, Type type) throws IOException {
        Collection<String> exceptionValues = response.headers().get(ExceptionHeader.RESPONSE_EXCEPTION_HEADER_NAME);
        if (exceptionValues != null
                && response.status() == HttpStatus.OK.value()
                && !exceptionValues.isEmpty()) {
            if (BaseResponseData.class.isAssignableFrom(type.getClass())
                    || Map.class.isAssignableFrom(type.getClass())) {
                return;
            }
            String exServiceHeader = exceptionValues.iterator().next();
            ThroughErrorCode throughErrorCode = (ThroughErrorCode) delegate.decode(response, ThroughErrorCode.class);
            throw new FeignThroughBusinessException(exServiceHeader, throughErrorCode.toErrorCode());
        }
    }

    boolean isOptional(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType.getRawType().equals(Optional.class);
        }
    }

    static class ThroughErrorCode {

        private int code;
        private String message;

        public void setCode(int code) {
            this.code = code;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ErrorCode toErrorCode() {
            return ErrorCode.build(code, message);
        }
    }
}
