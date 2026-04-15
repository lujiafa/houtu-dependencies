package io.github.lujiafa.houtu.limit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于Redis的分布式限流器
 * @author jon
 * @date 2024年2月25日
 */
public class RateLimiter {

    private Supplier<Boolean> tryAcquireSupplier;

    public static FixSlidingWindowBuilder fixSlidingWindow(RedisTemplate<String, ?> redisTemplate) {
        return new FixSlidingWindowBuilder(redisTemplate);
    }

    public static RollingSlidingWindowBuilder rollingSlidingWindow(RedisTemplate<String, ?> redisTemplate) {
        return new RollingSlidingWindowBuilder(redisTemplate);
    }

    /**
     * 获取通行许可
     *   1.如果当前有令牌：立即返回
     *   2.如果没有：阻塞等待，直到有令牌
     */
    public void acquire() {
        long sleepTime = 1; // 初始等待时间
        while (!tryAcquire()) {
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                sleepTime = Math.min(sleepTime << 1, 10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * 非阻塞尝试获取
     *   1.如果当前有令牌：立即返回true
     *   2.如果没有：立即返回false
     * @return  boolean 是否获取成功
     */
    public boolean tryAcquire() {
        return tryAcquireSupplier.get();
    }


    static String loadLuaFile(String fileName) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        Assert.notNull(inputStream, "Failed to load Lua file: " + fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (content.length() > 0) {
                    content.append("\n");
                }
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Lua file", e);
        }
    }

    public static abstract class SlidingWindowBuilder {
        static final GenericToStringSerializer<Long> ARGS_SERIALIZER = new GenericToStringSerializer<Long>(Long.class);

        protected RedisTemplate<String, ?> redisTemplate;
        protected String name = "default";
        protected int limit = 200;
        protected Duration windowSize = Duration.ofSeconds(10);

        public SlidingWindowBuilder(RedisTemplate<String, ?> redisTemplate) {
            Assert.notNull(redisTemplate, "redisTemplate must not be null");
            this.redisTemplate = redisTemplate;
        }

        public SlidingWindowBuilder name(String name) {
            Assert.notNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public SlidingWindowBuilder limit(int limit) {
            Assert.isTrue(limit > 0, "limit must be positive");
            this.limit = limit;
            return this;
        }

        public SlidingWindowBuilder windowSize(Duration windowSize) {
            Assert.notNull(windowSize, "windowSize must not be null");
            Assert.isTrue(windowSize.toMillis() > 0, "windowSize must be positive");
            this.windowSize = windowSize;
            return this;
        }

        public abstract RateLimiter build();
    }

    public static class FixSlidingWindowBuilder extends SlidingWindowBuilder {

        /**
         * 自然滑动窗口/固定滑动窗口 限流脚本对象。
         * |-----窗口1-----|-----窗口2-----|
         *   ↑ 自然窗口能保障限流，且在边界瞬间可以双倍通过（可能产生边界突刺）
         */
        static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT;
        static final String KEY_PATTERN = "fix:sliding:window:rate:limiter:%s:%d";

        static {
            SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>();
            SLIDING_WINDOW_SCRIPT.setResultType(Long.class);
            SLIDING_WINDOW_SCRIPT.setScriptText(loadLuaFile("lua/fix-sliding-window.lua"));
        }

        FixSlidingWindowBuilder(RedisTemplate<String, ?> redisTemplate) {
            super(redisTemplate);
        }

        public RateLimiter build() {
            RateLimiter slidingWindowRateLimiter = new RateLimiter();
            long windowSize = this.windowSize.toMillis();
            slidingWindowRateLimiter.tryAcquireSupplier = () -> {
                String key = String.format(KEY_PATTERN, name, System.currentTimeMillis() / windowSize);
                long ttl = Math.max(windowSize * 3 / 1000, 1); // 过期时间不能小于1秒，单位seconds
                List<String> keys = Collections.singletonList(key);
                return redisTemplate.execute(SLIDING_WINDOW_SCRIPT,
                        SlidingWindowBuilder.ARGS_SERIALIZER,
                        SlidingWindowBuilder.ARGS_SERIALIZER,
                        keys,
                        ttl) <= limit;
            };
            return slidingWindowRateLimiter;
        }
    }

    public static class RollingSlidingWindowBuilder extends SlidingWindowBuilder {

        /**
         * 滚动滑动窗口 限流脚本对象。
         * |-----窗口1-----|-----窗口2-----|
         *   ↑ 滚动滑动窗口保障每个时间限流均生效，限流精度高且能防止边界突刺，但性能相对差一些
         */
        static final DefaultRedisScript<Boolean> SLIDING_WINDOW_SCRIPT;
        static final String KEY_PATTERN = "zset:sliding:window:rate:limiter:%s";

        static final GenericToStringSerializer<Boolean> RESULT_SERIALIZER = new GenericToStringSerializer<Boolean>(Boolean.class);

        static {
            SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>();
            SLIDING_WINDOW_SCRIPT.setResultType(Boolean.class);
            SLIDING_WINDOW_SCRIPT.setScriptText(loadLuaFile("lua/zset-sliding-window.lua"));
        }

        RollingSlidingWindowBuilder(RedisTemplate<String, ?> redisTemplate) {
            super(redisTemplate);
        }

        public RateLimiter build() {
            RateLimiter slidingWindowRateLimiter = new RateLimiter();
            long windowSize = this.windowSize.toMillis();
            slidingWindowRateLimiter.tryAcquireSupplier = () -> {
                String key = String.format(KEY_PATTERN, name);
                long ttl = Math.max(windowSize * 3 / 1000, 1); // 过期时间不能小于1秒，单位seconds
                List<String> keys = Collections.singletonList(key);
                return redisTemplate.execute(SLIDING_WINDOW_SCRIPT,
                        SlidingWindowBuilder.ARGS_SERIALIZER,
                        RESULT_SERIALIZER,
                        keys,
                        System.currentTimeMillis(),
                        windowSize,
                        limit,
                        windowSize,
                        ttl);
            };
            return slidingWindowRateLimiter;
        }
    }
}
