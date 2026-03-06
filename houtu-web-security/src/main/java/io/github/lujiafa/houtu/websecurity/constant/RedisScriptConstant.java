package io.github.lujiafa.houtu.websecurity.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public interface RedisScriptConstant {

    /**
     * 删除会话时同步删除其互斥数据Lua
     */
    String SESSION_DEL_MUTEX_DATA_BY_LUA = "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
            "return redis.call('DEL', KEYS[1]) " +
            "else " +
            "return 0 " +
            "end";
    RedisScript<Long> SESSION_DEL_MUTEX_DATA_SCRIPT = new DefaultRedisScript<>(SESSION_DEL_MUTEX_DATA_BY_LUA, Long.class);
}
