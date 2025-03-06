-- KEYS[1]: 令牌桶的键名
-- ARGV[1]: 令牌桶容量
-- ARGV[2]: 令牌生成速率（每秒生成多少令牌）
-- ARGV[3]: 当前时间戳（毫秒）
-- ARGV[4]: 请求的令牌数

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

-- 获取上次更新时间和当前令牌数（如果不存在则初始化）
local last_time = redis.call('HGET', key, 'last_time')
local tokens = redis.call('HGET', key, 'tokens')

if last_time == false or tokens == false then
    last_time = now
    tokens = capacity
else
    last_time = tonumber(last_time)
    tokens = tonumber(tokens)
end

-- 计算自上次更新以来生成的令牌数
local elapsed = now - last_time
local new_tokens = elapsed * rate / 1000
tokens = math.min(capacity, tokens + new_tokens)

-- 更新时间戳
redis.call('HSET', key, 'last_time', now)

-- 检查是否有足够的令牌
if tokens >= requested then
    tokens = tokens - requested
    redis.call('HSET', key, 'tokens', tokens)
    return 1  -- 允许请求
else
    redis.call('HSET', key, 'tokens', tokens)
    return 0  -- 拒绝请求
end