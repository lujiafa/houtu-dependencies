package com.houtu.websecurity.prop;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

public class SessionRedisProperties extends RedisProperties {

    private Ssl ssl;

    private Jedis jedis;

    private Lettuce lettuce;

    @Override
    public Ssl getSsl() {
        return this.ssl == null ? super.getSsl() : this.ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

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
