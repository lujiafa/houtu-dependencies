package com.houtu.actuator.metrics.client;

import com.houtu.util.constant.CharConstant;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.util.StringUtils;

@Deprecated
public class DefaultHttpClient5ExecChainTagsProvider implements HttpClient5ExecChainTagsProvider {
    @Override
    public Iterable<Tag> getTags(ClassicHttpRequest classicHttpRequest, ExecChain.Scope scope, String metricUri, ClassicHttpResponse response, Throwable throwable) {
        String scheme = classicHttpRequest.getScheme();
        String host = classicHttpRequest.getAuthority() != null ? classicHttpRequest.getAuthority().getHostName() : "UNKNOWN";
        int port = classicHttpRequest.getAuthority() != null ? classicHttpRequest.getAuthority().getPort() : -1;
        String serverName = host + (port > 0 ? ":" + port : "");
        String svrname = scheme != null ? scheme + "://" + serverName : serverName;
        if (CharConstant.EMPTY.equals(metricUri)) {
            metricUri = classicHttpRequest.getRequestUri();
            int queryIndex = metricUri.indexOf('?');
            if (queryIndex > 0) {
                metricUri = metricUri.substring(0, queryIndex);
            }
            int fragmentIndex = metricUri.indexOf('#');
            if (fragmentIndex > 0) {
                metricUri = metricUri.substring(0, fragmentIndex);
            }
        }
        return Tags.of("svrname", svrname).and("method", classicHttpRequest.getMethod()).and("uri", metricUri).and(status(response)).and(exception(throwable));
    }

    public static Tag status(ClassicHttpResponse response) {
        return Tag.of("status", response != null ? Integer.toString(response.getCode()) : "UNKNOWN");
    }

    public static Tag exception(Throwable throwable) {
        if (throwable != null) {
            String simpleName = throwable.getClass().getSimpleName();
            return Tag.of("exception", StringUtils.hasText(simpleName) ? simpleName : throwable.getClass().getName());
        }
        return Tag.of("exception", "None");
    }
}
