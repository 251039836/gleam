package gleam.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内存统计日志
 * 
 * @author hdh
 *
 */
public class MemoryLogUtil {

    private final static Logger logger = LoggerFactory.getLogger("STATLOG");
    public final static int KB = 1024;
    public final static int MB = KB * 1024;

    /**
     * 打印gc次数
     */
    public static void printGcInfo() {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : garbageCollectorMXBeans) {
            String name = gc.getName();
            long collectionCount = gc.getCollectionCount();
            long collectionTime = gc.getCollectionTime();
            logger.info("[{}] collection count={},time={}", name, collectionCount, collectionTime);
        }
    }

    /**
     * 打印内存信息
     * 
     */
    public static void printMemoryInfo() {
        MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memorymbean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memorymbean.getNonHeapMemoryUsage();
        // init 初始值 xms
        // max 最大值 xmx
        // used 当前实际使用值
        // committed 当前可使用值(已被jvm申请到的内存)
        long heapUsed = heapMemoryUsage.getUsed() / MB;
        long heapCommitted = heapMemoryUsage.getCommitted() / MB;
        long nonHeapUsed = nonHeapMemoryUsage.getUsed() / MB;
        long nonHeapCommitted = nonHeapMemoryUsage.getCommitted() / MB;

        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
        totalMemory = (long) Math.ceil(totalMemory * 1d / MB);
        usedMemory = (long) Math.ceil(usedMemory * 1d / MB);

        logger.info("memory={}/{}mb.heap={}/{}mb,nonHeap={}/{}mb.", usedMemory, totalMemory, heapUsed, heapCommitted, nonHeapUsed, nonHeapCommitted);
    }
}
