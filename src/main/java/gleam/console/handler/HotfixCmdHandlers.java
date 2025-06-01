package gleam.console.handler;

import java.util.function.Function;

import gleam.console.ConsoleCmdHandler;
import gleam.util.HotfixUtil;

/**
 *
 * 热更，修复命令处理器
 *
 * @author redback
 * @version 1.00
 * @time 2020-4-27 14:57
 */
public enum HotfixCmdHandlers implements ConsoleCmdHandler {

    /**
     * 重写加载配置
     */
    RELOAD("reload", (cmd) -> HotfixUtil.reloadJson(cmd)),

    /**
     * 热修复
     */
    HOT_FIX("hotfix", (cmd) -> HotfixUtil.hotswapClass()),

    /**
     * 运行脚本
     */
    RUN_SCRIPT("runScript", (cmd) -> HotfixUtil.runScript(cmd)),
    
    /**
     * 打印版本号
     */
    VERSION("version", (cmd) -> HotfixUtil.version()),
    ;

    private String type;

    private Function<String, String> handler;

    HotfixCmdHandlers(String type, Function<String, String> handler) {
        this.type = type;
        this.handler = handler;
    }

    @Override
    public String getCmdType() {
        return this.getType();
    }

    public Function<String, String> getHandler() {
        return handler;
    }

    public String getType() {
        return type;
    }

    @Override
    public String handler(String cmd) {
        return this.handler.apply(cmd);
    }
}
