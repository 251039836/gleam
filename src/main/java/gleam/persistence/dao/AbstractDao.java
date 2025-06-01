package gleam.persistence.dao;

import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.persistence.Dao;
import gleam.persistence.PersistenceData;
import gleam.persistence.annotation.TableName;
import gleam.persistence.dbutils.JdbcManager;

/**
 * 使用hikariCp+dbUtils实现的简单dao
 * 
 * @author hdh
 *
 * @param <T>
 */
public abstract class AbstractDao<T extends PersistenceData> implements Dao<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 操作过长时间 毫秒<br>
     * 若操作时间过长 则打印日志
     */
    protected final static long WARN_TIME = TimeUnit.MILLISECONDS.toMillis(500);
    /**
     * 该dao对应的数据类的class
     */
    protected final Class<T> dataClazz;

    protected final String tableName;

    public AbstractDao(Class<T> dataClazz) {
        this.dataClazz = dataClazz;
        TableName tableName = dataClazz.getAnnotation(TableName.class);
        if (tableName == null) {
            throw new IllegalArgumentException("dataClazz[" + dataClazz.getName() + "] tableName is null.");
        }
        this.tableName = tableName.value();
    }

    public AbstractDao(Class<T> dataClazz, String tableName) {
        this.dataClazz = dataClazz;
        this.tableName = tableName;
    }

    @Override
    public Class<T> getDataClazz() {
        return dataClazz;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    protected void logCostTime(String sql, long startTime) {
        long now = System.currentTimeMillis();
        long costTime = now - startTime;
        if (costTime >= WARN_TIME) {
            logger.warn("excute sql[{}] costTime:{}ms", sql, costTime);
        }
    }

    protected QueryRunner getQueryRunner() {
        return JdbcManager.getInstance().getQueryRunner();
    }

}
