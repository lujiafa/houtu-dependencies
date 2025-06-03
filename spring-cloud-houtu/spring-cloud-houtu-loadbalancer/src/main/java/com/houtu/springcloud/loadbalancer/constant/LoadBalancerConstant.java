package com.houtu.springcloud.loadbalancer.constant;

public interface LoadBalancerConstant {

    /**
     * 注册中心元数据HINT名称
     */
    String METADATA_HINT_NAME = "hint";

    /**
     * 请求链路中传递的HINT名称
     */
    String REQUEST_CONTEXT_HINT_NAME = "X-Hint";
}
