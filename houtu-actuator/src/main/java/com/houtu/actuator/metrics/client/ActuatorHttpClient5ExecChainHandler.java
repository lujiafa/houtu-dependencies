package com.houtu.actuator.metrics.client;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.slf4j.Logger;
import org.springframework.boot.actuate.metrics.AutoTimer;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Deprecated
public class ActuatorHttpClient5ExecChainHandler implements ExecChainHandler {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(ActuatorHttpClient5ExecChainHandler.class);

    private MeterRegistry meterRegistry;
    private HttpClient5ExecChainTagsProvider tagsProvider;

    private String metricName;
    private final AutoTimer autoTimer;

    public ActuatorHttpClient5ExecChainHandler(MeterRegistry meterRegistry, String metricName, AutoTimer autoTimer, HttpClient5ExecChainTagsProvider tagsProvider) {
        this.meterRegistry = meterRegistry;
        this.metricName = metricName;
        this.autoTimer = autoTimer != null ? autoTimer : AutoTimer.DISABLED;
        this.tagsProvider = tagsProvider;
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest classicHttpRequest, ExecChain.Scope scope, ExecChain execChain) throws IOException, HttpException {
        String metricUri = HttpClientMetric.HTTP_CLIENT.get();
        if (metricUri == null) {
            return execChain.proceed(classicHttpRequest, scope);
        }
        long startTime = System.nanoTime();
        ClassicHttpResponse response = null;
        Throwable throwable = null;
        try {
            return (response = execChain.proceed(classicHttpRequest, scope));
        } catch (Throwable t) {
            throwable = unwrapNestedServletException(t);
            throw t;
        } finally {
            try {
                HttpClientMetric.HTTP_CLIENT.remove();
                getTimeBuilder(classicHttpRequest, scope, metricUri, response, throwable).register(this.meterRegistry)
                        .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            } catch (Exception ex) {
                logger.info("Failed to record metrics.", ex);
            }
        }
    }

    private Timer.Builder getTimeBuilder(ClassicHttpRequest classicHttpRequest, ExecChain.Scope scope, String metricUri, ClassicHttpResponse response, Throwable throwable) {
        return this.autoTimer.builder(this.metricName)
                .tags(tagsProvider.getTags(classicHttpRequest, scope, metricUri, response, throwable))
                .description("Timer of http client request operation");
    }

    private Throwable unwrapNestedServletException(Throwable ex) {
        return (ex instanceof NestedServletException) ? ex.getCause() : ex;
    }
}
