package io.github.lujiafa.houtu.actuator.metrics.client;

import io.micrometer.core.instrument.Tag;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;

@Deprecated
public interface HttpClient5ExecChainTagsProvider {

    Iterable<Tag> getTags(ClassicHttpRequest classicHttpRequest, ExecChain.Scope scope, String metricUri, ClassicHttpResponse response, Throwable exception);

}
