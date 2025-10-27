package com.houtu.websecurity.prop;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

public class SessionRedisProperties extends RedisProperties {

    private Jedis jedis;

    private Lettuce lettuce;

    @Override
    public Jedis getJedis() {
        return this.jedis == null ? super.getJedis() : this.jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public Lettuce getLettuce() {
        return this.lettuce == null ? super.getLettuce() : this.lettuce;
    }

    public void setLettuce(Lettuce lettuce) {
        this.lettuce = lettuce;
    }

}
