package gleam.persistence.saver;

import gleam.persistence.define.PersistenceConstant;

/**
 * 待保存的数据夹子<br>
 * 
 * @author hdh
 *
 * @param <T>
 */
public class DataClip<T> {

    /**
     * 待保存的数据对象
     */
    private T data;
    /**
     * 保存失败次数<br>
     * 每保存失败一次 下次尝试保存的间隔会延长
     */
    private int errorCount;
    /**
     * tick次数<br>
     * 每保存失败一次 重置为0重新开始计算
     */
    private byte tickCount;

    public DataClip() {
    }

    public DataClip(T data) {
        this.data = data;
    }

    /**
     * 增加错误次数<br>
     * 重置tick次数
     */
    public void addErrorCount() {
        tickCount = 0;
        errorCount++;
    }

    @Override
    public boolean equals(Object obj) {
        // 用于remove时 判断是否同1个实例
        return (this == obj);
    }

    public T getData() {
        return data;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public byte getTickCount() {
        return tickCount;
    }

    /**
     * 是否到时间进行保存操作
     * 
     * @return
     */
    public boolean isTimeToSave() {
        tickCount++;
        if (tickCount >= errorCount || tickCount >= PersistenceConstant.MAX_BACKOFF_TICK_COUNT) {
            return true;
        }
        return false;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public void setTickCount(byte tickCount) {
        this.tickCount = tickCount;
    }
}
