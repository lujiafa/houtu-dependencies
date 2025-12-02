package com.houtu.actuator.metrics.client;

import com.houtu.util.constant.CharConstant;

public class HttpClientMetric {

    static ThreadLocal<String> HTTP_CLIENT = new ThreadLocal<>();

    public static void metric() {
        metric(null);
    }

    /**
     * 设置当前线程的HttpClient5请求的metricUri
     * <p>
     * <strong>防止访问API地址为PathVariable类型时产生大量metric指标导致OOM</strong>，当访问API地址为PathVariable时，请注意通过此方法自定义metricUri
     * </p>
     *
     * @param metricUri 自定义的metric URI
     */
    public static void metric(String metricUri) {
        if (metricUri == null) {
            metricUri = CharConstant.EMPTY;
        }
        HTTP_CLIENT.set(metricUri);
    }

}
