package gleam.communication.echo.manager;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gleam.communication.echo.BackstageMsgHandler;
import gleam.communication.echo.protocol.BackstageHandlerResp;
import gleam.communication.echo.protocol.GmMessage;
import gleam.config.backstage.RefreshRemoteConfigMsgHandler;
import gleam.core.service.AbstractService;
import gleam.exception.ServerStarupError;
import gleam.util.ClazzUtil;
import gleam.util.json.JsonUtil;

/**
 * 后台消息处理管理器，格式为 json 数据 处理 json 命令，格式为 {"protocol":1001, "content":{xxxxx}}
 * protocol 为约定的协议 id, 用于命令的分发 content 为具体协议的数据内容， 为 json 字符串 处理器会字段将 content 中的
 * json 字符串反序列化为指定的对象， 该对象类型由 JsonHandler 接口实现类中的泛型类类型确定
 *
 * @author redback
 * @version 1.00
 * @time 2020-5-8 16:30
 */
public class BackstageManager extends AbstractService {

    private static final BackstageManager instance = new BackstageManager();

    public static BackstageManager getInstance() {
        return instance;
    }

    /**
     * 协议 Id -> 后台消息处理器
     */
    private Map<Integer, BackstageMsgHandler<?>> id2Handlers = new HashMap<>();

    private BackstageManager() {
    }

    /**
     * 处理后台消息
     * 
     * @param msg 结构参考 "{"protocol":1001, "content": {xxx}}" 对应数据结构
     *            {@link GmMessage}
     * @param <T> content 解析之后的数据类型
     * @return 返还结果 {@link BackstageHandlerResp}
     * @throws JsonProcessingException Json 序列化错误
     */
    @SuppressWarnings("unchecked")
    public <T> String handler(String msg) {
        logger.info("receive msg:{}", msg);
        BackstageHandlerResp response;
        try {
            GmMessage gmMessage = JsonUtil.toObject(msg, GmMessage.class);
            if (gmMessage == null) {
                String resp = String.format(" 消息[%s]格式有误，消息无法反序列化", msg);
                logger.error(resp);
                response = BackstageHandlerResp.failed(resp);
                return JsonUtil.toJson(response);
            }
            BackstageMsgHandler<T> backstageMsgHandler = (BackstageMsgHandler<T>) id2Handlers.get(gmMessage.getProtocol());
            if (backstageMsgHandler == null) {
                String resp = String.format("无法处理 protocol Id : [%d] 消息", gmMessage.getProtocol());
                logger.error(resp);
                response = BackstageHandlerResp.failed(resp);
                return JsonUtil.toJson(response);
            }
            T targetMsg = null;
            String content = gmMessage.getContent();
            if (!StringUtils.isEmpty(content)) {
                Class<?>[] classes = ClazzUtil.getParameterizedTypeClazzes(backstageMsgHandler.getClass(), BackstageMsgHandler.class);
                assert classes != null;
                Class<T> targetType = (Class<T>) classes[0];
                if (targetType != null && !targetType.equals(Void.class)) {
                    targetMsg = JsonUtil.toObject(content, targetType);
                }
            }
            response = backstageMsgHandler.handler(targetMsg);
        } catch (Exception e) {
            logger.error("handler msg error.", e);
            response = BackstageHandlerResp.failed("服务器发生内部错误，请查看日志查询问题");
        }
        return JsonUtil.toJson(response);
    }

    @Override
    protected void onInitialize() throws Exception {
        scanBackstageMsgHandlers();
    }

    /**
     * 注册 后台消息处理器
     * 
     * @param backstageMsgHandler
     */
    private void registerJsonHandler(BackstageMsgHandler<?> backstageMsgHandler) {
        BackstageMsgHandler<?> oldBackstageMsgHandler = id2Handlers.put(backstageMsgHandler.getId(), backstageMsgHandler);
        if (oldBackstageMsgHandler != null) { // 如果处理器重复，直接抛出异常
            logger.error("json handler id:[{}] repeated,old handler: [{}],  new handler : [{}]", backstageMsgHandler.getId(),
                    oldBackstageMsgHandler.getClass().getCanonicalName(), backstageMsgHandler.getClass().getCanonicalName());
            throw new ServerStarupError("json handler register repeated ! ");
        }
    }

    /**
     * 扫描接口实现类
     * 
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    private void scanBackstageMsgHandlers() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // 当前包
        registerJsonHandler(RefreshRemoteConfigMsgHandler.getInstance());
        // 扫描 game.com
        @SuppressWarnings("rawtypes")
        List<BackstageMsgHandler> backstageMsgHandlers = ClazzUtil.scanImplAndNewInstances(ClazzUtil.GAME_PACKAGE_NAME, BackstageMsgHandler.class);
        for (BackstageMsgHandler<?> backstageMsgHandler : backstageMsgHandlers) {
            registerJsonHandler(backstageMsgHandler);
        }
    }

}
