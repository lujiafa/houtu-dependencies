local key = KEYS[1]
local ct = tonumber(ARGV[1])
local wz = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local minttl = tonumber(ARGV[4])
local ex = tonumber(ARGV[5])
redis.call('ZREMRANGEBYSCORE', key, 0, ct - wz)
local count = redis.call('ZCARD', key)
if count >= limit then
    return 0
end
redis.call('ZADD', key, ct, ct .. "-" .. math.random())
if redis.call('TTL', key) < minttl then
    redis.call('EXPIRE', key, ex)
end
return 1