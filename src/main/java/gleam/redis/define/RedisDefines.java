package gleam.redis.define;

import java.util.concurrent.TimeUnit;

public class RedisDefines {

    public static final String SERVER_REDIS_YML_FILE = "redis.yml";

    public final static String REDIS_HOST_KEY = "redis.server.host";
    public final static String REDIS_PORT_KEY = "redis.server.port";
    public final static String REDIS_PASSWORD_KEY = "redis.server.password";
    public final static String REDIS_DATABASE_KEY = "redis.server.database";

    /**
     * 计时任务间隔时间, 默认5分钟一次
     */
    public static final long TICK_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    /**
     * 请求redis服务器响应超时时间, 3秒, 超过3秒表示无响应,redis服务器可能为断开状态
     */
    public static final long PING_TIMEOUT = TimeUnit.SECONDS.toMillis(3);

}
