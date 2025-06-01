package gleam.redis;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.api.redisnode.RedisSingle;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import gleam.communication.task.CommunicationTaskManager;
import gleam.config.ServerSettings;
import gleam.core.service.AbstractService;
import gleam.exception.ServerStarupError;
import gleam.redis.define.RedisDefines;
import gleam.util.ClazzUtil;
import gleam.util.ResourceUtil;

/**
 * redis服务类<br>
 * 客户端, 封装redisson连接
 * 
 * @author Jeremy
 */
public class RedisService extends AbstractService {

    public static RedisService instance = new RedisService();

    public static RedisService getInstance() {
        return instance;
    }

    /**
     * redis客户端
     */
    private RedissonClient redissonClient;
    /**
     * key: pojo实体类<V> value: IRedisTemplate<K,V>
     */
    private final Map<String, RedisRepostory<?, ?>> repostoryMap = new HashMap<>();

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    @Override
    public int getPriority() {
        return PRIORITY_HIGHEST;
    }

    @Override
    protected void onInitialize() throws Exception {
        this.initSingleClient();
        this.initRepostory();
    }

    private void initSingleClient() throws IOException {
        // 优先使用conf内的配置
        String host = ServerSettings.getProperty(RedisDefines.REDIS_HOST_KEY);
        String port = ServerSettings.getProperty(RedisDefines.REDIS_PORT_KEY);
        String password = ServerSettings.getProperty(RedisDefines.REDIS_PASSWORD_KEY);
        int database = ServerSettings.getIntProperty(RedisDefines.REDIS_DATABASE_KEY, 0);
        if (StringUtils.isAnyBlank(host, port)) {
            // 地址地址为空, 不允许连接
            throw new ServerStarupError("The address of the redis is null, host:" + host + ", port:" + port);
        }
        URL url = ResourceUtil.getSettingsFileUrl(RedisDefines.SERVER_REDIS_YML_FILE);
        Config config = Config.fromYAML(url);
        SingleServerConfig singleConfig = config.useSingleServer();
        String address = "redis://".concat(host).concat(":").concat(port);
        singleConfig.setAddress(address).setDatabase(database);
        if (!StringUtils.isBlank(password)) {
            singleConfig.setPassword(password);
        }
        this.redissonClient = Redisson.create(config);
    }

    @SuppressWarnings("rawtypes")
    private void initRepostory() {
        try {
            List<RedisRepostory> repostoryList = ClazzUtil.scanImplAndNewInstances(ClazzUtil.GAME_PACKAGE_NAME, RedisRepostory.class);
            repostoryList.forEach(repostory -> {
                repostory.init();
                repostoryMap.put(repostory.getCacheName(), repostory);// 实体类->template
            });
        } catch (Exception e) {
            throw new ServerStarupError("redisService init respostory error.", e);
        }
    }

    @Override
    protected void onStart() throws Exception {
        super.onStart();
        this.startTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mergeAll();
    }

    @Override
    public void onDestroy() {
        redissonClient.shutdown();
    }

    /**
     * 开始一个新任务
     */
    private void startTask() {
        CommunicationTaskManager.TIMER.scheduleTaskStartAtNextTimeUnit(() -> {
            this.mergeAll();
        }, RedisDefines.TICK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * 检测redis server是否处于active状态, 通过发送ping命令检测,
     * 
     * @return
     */
    private boolean checkActive() {
        RedisSingle node = getRedissonClient().getRedisNodes(RedisNodes.SINGLE);
        boolean active = node.pingAll(RedisDefines.PING_TIMEOUT, TimeUnit.MILLISECONDS);
        return active;
    }

    /**
     * 用于处理合并所有数据
     */
    private void mergeAll() {
        if (!checkActive()) {// 阻塞获取
            return;
        }
        repostoryMap.forEach((key, val) -> {
            val.mergeAll();
        });
    }

    public Map<String, RedisRepostory<?, ?>> getRepostoryMap() {
        return repostoryMap;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void registerRepostory(RedisRepostory<?, ?> repostory) {
        repostoryMap.put(repostory.getCacheName(), repostory);
    }
}
