package gleam.persistence.define;

import java.util.concurrent.TimeUnit;

public class PersistenceConstant {

    public final static long DEFAULT_SAVER_TICK_INTERVAL = TimeUnit.SECONDS.toMillis(30);
    /**
     * 最大退避次数<br>
     * 尝试保存间隔上限<br>
     * 数据每保存失败一次 则增加尝试间隔<br>
     */
    public final static int MAX_BACKOFF_TICK_COUNT = 60;
    /**
     * 关服时 无法保存的数据写文件保存 路径
     */
    public final static String ERROR_DATA_FILE_DIR = "backups";
}
