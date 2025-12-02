package com.houtu.actuator.metrics.client;

import com.houtu.util.constant.CharConstant;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.RequestReplySenderContext;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.slf4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.Locale;

public class ActuatorHttpClient5ExecChainHandlerObservation implements ExecChainHandler {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(ActuatorHttpClient5ExecChainHandlerObservation.class);

    private ObservationRegistry observationRegistry;
    private HttpClient5ObservationConvention observationConvention;

    public ActuatorHttpClient5ExecChainHandlerObservation(ObservationRegistry observationRegistry, HttpClient5ObservationConvention observationConvention) {
        this.observationRegistry = observationRegistry;
        this.observationConvention = observationConvention;
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain execChain) throws IOException, HttpException {
        String metricUri = HttpClientMetric.HTTP_CLIENT.get();
        if (metricUri == null) {
            return execChain.proceed(request, scope);
        }
        Observation observation = Observation.createNotStarted(observationConvention, () -> new HttpClient5RequestReplySenderContext(request, metricUri), observationRegistry).start();
        try {
            Observation.Scope observationScope = observation.openScope();
            try {
                ClassicHttpResponse response = execChain.proceed(request, scope);
                ((HttpClient5RequestReplySenderContext) observation.getContext()).setResponse(response);
                if (scope != null) {
                    observationScope.close();
                }
                return response;
            } catch (Throwable t) {
                if (observationScope != null) {
                    try {
                        observationScope.close();
                    } catch (Throwable tv) {
                        t.addSuppressed(tv);
                    }
                }
                throw t;
            }
        } catch (Throwable t) {
            Throwable throwable = unwrapNestedServletException(t);
            observation.error(throwable);
            throw t;
        } finally {
            try {
                HttpClientMetric.HTTP_CLIENT.remove();
                observation.stop();
            } catch (Exception ex) {
                logger.info("Failed to record metrics.", ex);
            }
        }
    }

    private Throwable unwrapNestedServletException(Throwable ex) {
        return (ex instanceof NestedServletException) ? ex.getCause() : ex;
    }

    static class HttpClient5RequestReplySenderContext extends RequestReplySenderContext<ClassicHttpRequest, ClassicHttpResponse> {

        private String metricUri;

        public HttpClient5RequestReplySenderContext(ClassicHttpRequest request, String metricUri) {
            super(HttpClient5RequestReplySenderContext::setRequestHeader);
            this.setCarrier(request);
            this.metricUri = metricUri;
        }

        private static void setRequestHeader(@Nullable ClassicHttpRequest request, String name, String value) {
            if (request != null) {
                request.setHeader(name, value);
            }
        }

        public String getMetricUri() {
            return metricUri;
        }
    }

    public static class HttpClient5ObservationConvention implements ObservationConvention<HttpClient5RequestReplySenderContext> {

        private String name;

        public HttpClient5ObservationConvention(String name) {
            Assert.hasText(name, "name must not be empty");
            this.name = name;
        }


        @Override
        public KeyValues getLowCardinalityKeyValues(HttpClient5RequestReplySenderContext context) {
            return getBasicKeyValues(context.getCarrier(), context.getMetricUri()).and(status(context.getResponse())).and(exception(context.getError()));
        }

        @Override
        public KeyValues getHighCardinalityKeyValues(HttpClient5RequestReplySenderContext context) {
            return getBasicKeyValues(context.getCarrier(), context.getMetricUri());
        }

        public static KeyValue status(ClassicHttpResponse response) {
            return KeyValue.of("status", response != null ? Integer.toString(response.getCode()) : "UNKNOWN");
        }

        public static KeyValue exception(Throwable throwable) {
            if (throwable != null) {
                String simpleName = throwable.getClass().getSimpleName();
                return KeyValue.of("exception", StringUtils.hasText(simpleName) ? simpleName : throwable.getClass().getName());
            }
            return KeyValue.of("exception", "none");
        }

        public KeyValues getBasicKeyValues(ClassicHttpRequest request, String metricUri) {
            String scheme = request.getScheme();
            String host = request.getAuthority() != null ? request.getAuthority().getHostName() : "UNKNOWN";
            int port = request.getAuthority() != null ? request.getAuthority().getPort() : -1;
            String serverName = host + (port > 0 ? ":" + port : "");
            String svrname = scheme != null ? scheme + "://" + serverName : serverName;
            if (CharConstant.EMPTY.equals(metricUri)) {
                metricUri = request.getRequestUri();
                int queryIndex = metricUri.indexOf('?');
                if (queryIndex > 0) {
                    metricUri = metricUri.substring(0, queryIndex);
                }
                int fragmentIndex = metricUri.indexOf('#');
                if (fragmentIndex > 0) {
                    metricUri = metricUri.substring(0, fragmentIndex);
                }
            }
            return KeyValues.of("svrname", svrname, "method", request.getMethod(), "uri", metricUri);
        }

        @Override
        public boolean supportsContext(Observation.Context context) {
            return context instanceof HttpClient5RequestReplySenderContext;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getContextualName(HttpClient5RequestReplySenderContext context) {
            ClassicHttpRequest request = (ClassicHttpRequest) context.getCarrier();
            return request != null ? "http " + request.getMethod().toLowerCase(Locale.ROOT) : null;
        }
    }
}
