package gleam.console;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 * 控制台命令处理接口
 *
 * @author redback
 * @version 1.00
 * @time 2020-4-26 15:21
 */
public interface ConsoleCmdHandler {

    /**
     * 获取平台消息处理类型
     * 
     * @return cmdType
     */
    String getCmdType();

    /**
     * 处理平台命令
     * 
     * @param cmd 平台指令内容
     * @return 执行返回结果
     */
    String handler(String cmd) throws JsonProcessingException;

}
