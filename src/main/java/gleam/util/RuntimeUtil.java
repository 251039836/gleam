package gleam.util;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.task.TaskManager;

/**
 * @author redback
 * @version 1.00
 * @time 2020-4-27 17:20
 */
public class RuntimeUtil {

    private final static Logger logger = LoggerFactory.getLogger(RuntimeUtil.class);

    /**
     * 关闭服务器
     * 
     * @return result
     */
    public static String exit() {
        String result = "close server is executed";
        String name = ManagementFactory.getRuntimeMXBean().getName();
        logger.info("服务器即将关闭， pid=" + name.split("@")[0]);
        TaskManager.getInstance().scheduleTask(() -> System.exit(0), 3000L);
        return result;
    }

    /**
     * 手动执行 FullGC
     * 
     * @return result
     */
    public static String gc() {
        String result = "gc success";
        try {
            MemoryLogUtil.printMemoryInfo();
            System.gc();
            MemoryLogUtil.printGcInfo();
            MemoryLogUtil.printMemoryInfo();
        } catch (Exception e) {
            logger.error("gc error.", e);
            result = "gc fail";
        }
        return result;
    }

}
