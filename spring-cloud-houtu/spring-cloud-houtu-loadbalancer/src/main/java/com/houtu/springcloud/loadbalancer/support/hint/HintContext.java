package com.houtu.springcloud.loadbalancer.support.hint;

public final class HintContext {

    static final ThreadLocal<InnerHintData> THREAD_LOCAL = new ThreadLocal<InnerHintData>();

    public static InnerHintData get() {
        InnerHintData innerHintData = THREAD_LOCAL.get();
        if (innerHintData == null) {
            THREAD_LOCAL.set(innerHintData = new InnerHintData());
        }
        return innerHintData;
    }

    /**
     * 设置代码级自定义预期HINT，仅对当前线程有效
     * @param value 线程代码级预期HINT
     */
    public static void set(String value) {
        get().setHint(value);
    }

    /**
     * 设置调用链路级HINT，设置后会覆盖请求上游传递的HINT，并传递给请求下游服务
     * @param value 请求链路级预期HINT
     */
    public static void setX(String value) {
        get().setHint(value);
    }

    /**
     * 清空代码级和请求链路透传预期HINT
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }

    static class InnerHintData {
        /** 代码级自定义预期HINT **/
        private String hint;
        /** 请求链路透传预期HINT数据 **/
        private String xHint;

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

        public String getXHint() {
            return xHint;
        }

        public void setXHint(String xHint) {
            this.xHint = xHint;
        }
    }

}
