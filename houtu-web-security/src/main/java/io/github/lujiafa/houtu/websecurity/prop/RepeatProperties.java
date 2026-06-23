package io.github.lujiafa.houtu.websecurity.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = RepeatProperties.PREFIX)
public class RepeatProperties {

    public static final String PREFIX = "houtu.web.repeat";

    private static final int DEFAULT_EXPIRE = 900;

    /**
     * 防重放时间窗口，默认 15 分钟（900 秒）
     */
    private Duration expire = Duration.ofSeconds(DEFAULT_EXPIRE);

    public Duration getExpire() {
        return expire;
    }

    public void setExpire(Duration expire) {
        this.expire = expire;
    }
}
