package gleam.persistence;

/**
 * 可变数据<br>
 * 用于标记相关数据保存后是否发生过<br>
 * 减少未变化数据存库的操作
 * 
 * @author hdh
 *
 */
public interface Changeable {

    /**
     * 数据是否有变化
     * 
     * @return
     */
    boolean isChange();

    /**
     * 设置数据是否有变化<br>
     * 
     * @param change
     */
    void setChange(boolean change);

}
