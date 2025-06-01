package gleam.console.handler;

import com.fasterxml.jackson.core.JsonProcessingException;

import gleam.communication.echo.manager.BackstageManager;
import gleam.console.ConsoleCmdHandler;

/**
 * 处理后台的 gm 协议，具体功能和一恋项目一样 处理的 echo 命令格式 gm#{"protocol":1001, "content":{xxxxx}}
 *
 * 实际处理为 json 数据的命令 {"protocol":1001, "content":{xxxxx}} 具体如何处理，查看
 * JsonHandlerManager 类
 * 
 * @author redback
 * @version 1.00
 * @time 2020-4-26 18:18
 */
public class GmConsoleCmdHandler implements ConsoleCmdHandler {

    @Override
    public String getCmdType() {
        return "gm";
    }

    @Override
    public String handler(String cmd) throws JsonProcessingException {
        return BackstageManager.getInstance().handler(cmd);
    }

}
