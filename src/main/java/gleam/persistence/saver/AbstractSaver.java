package gleam.persistence.saver;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.persistence.PersistenceData;
import gleam.persistence.Saver;
import gleam.persistence.SaverManager;
import gleam.persistence.define.PersistenceConstant;
import gleam.task.TaskHandle;
import gleam.util.json.JsonUtil;

public abstract class AbstractSaver<T> implements Saver<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 定时任务间隔
     */
    protected final long tickInterval;
    /**
     * 定时任务
     */
    protected TaskHandle tickTask;

    public AbstractSaver() {
        this(PersistenceConstant.DEFAULT_SAVER_TICK_INTERVAL);
    }

    public AbstractSaver(long tickInterval) {
        if (tickInterval <= 0) {
            throw new IllegalArgumentException("tick interval <=0.");
        }
        this.tickInterval = tickInterval;
    }

    @Override
    public void startup() {
        startTickTask();
    }

    @Override
    public void shutdown() {
        cancelTickTask();
        doSaveAll(true);
    }

    protected void tick() {
        doSaveAll(false);
    }

    protected void startTickTask() {
        cancelTickTask();
        tickTask = SaverManager.getInstance().getTaskManager().scheduleTask(() -> {
            tick();
        }, tickInterval, tickInterval);
    }

    protected void cancelTickTask() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    public long getTickInterval() {
        return tickInterval;
    }

    public void setTickTask(TaskHandle tickTask) {
        this.tickTask = tickTask;
    }

    public TaskHandle getTickTask() {
        return tickTask;
    }

    protected abstract void doSaveAll(boolean shutdown);

    @Override
    public boolean save(T data, boolean block) throws Exception {
        if (block) {
            return blockSave(data);
        } else {
            asyncSave(data);
            return true;
        }
    }

    /**
     * 将数据存到本地文件中
     * 
     * @param data
     */
    protected void save2File(PersistenceData data) {
        if (data == null) {
            return;
        }
        Object primaryKey = data.getPrimaryKey();
        try {
            // 生成到logs目录下的data.clazz
            long now = System.currentTimeMillis();
            String dir = getErrorDataDir();
            String fileName = primaryKey + "_" + now;
            String json = JsonUtil.toJson(data);
            File file = new File(dir, fileName);
            file.mkdirs();
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("save faildata[{}] to file error.", primaryKey, e);
        }
    }

    /**
     * 关服时 无法保存的数据写文件保存 路径
     * 
     * @return
     */
    protected String getErrorDataDir() {
        return PersistenceConstant.ERROR_DATA_FILE_DIR + File.separator + getBeanClazz().getSimpleName();
    }

}
