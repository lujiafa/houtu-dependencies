package io.github.lujiafa.houtu.websecurity.prop;

import io.github.lujiafa.houtu.websecurity.constant.SecurityConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = RepeatProperties.PREFIX)
public class RepeatProperties {

    public static final String PREFIX = "houtu.web.repeat";

    private static final int DEFAULT_EXPIRE = 900;

    /**
     * 防重放时间窗口，默认 15 分钟（900 秒）
     */
    private Duration expire = Duration.ofSeconds(DEFAULT_EXPIRE);

    /**
     * 防重放参与 key 的字段列表，默认 nonce + timestamp + sign；配置为空列表表示不做防重放
     */
    private List<String> fields = new ArrayList<>(Arrays.asList(
            SecurityConstant.PARAM_NONCE_NAME,
            SecurityConstant.PARAM_TIMESTAMP_NAME,
            SecurityConstant.PARAM_SIGNATURE_NAME));

    /**
     * 防重放字段取值来源，默认两者都取（先取请求头，取不到再取请求参数）
     */
    private Source source = Source.BOTH;

    public Duration getExpire() {
        return expire;
    }

    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}
