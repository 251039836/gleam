package gleam.persistence.dao;

import java.util.Collection;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import gleam.persistence.PersistenceData;
import gleam.persistence.dbutils.BeanFieldDescriptor;
import gleam.persistence.dbutils.DefaultBeanProcessor;
import gleam.persistence.dbutils.DefaultRowProcessor;

/**
 * 使用dbutils作为默认实现的dao
 * 
 * @author hdh
 *
 * @param <T>
 */
public class BaseDao<T extends PersistenceData> extends AbstractDao<T> {

    protected DefaultBeanProcessor<T> beanProcessor;

    protected BeanHandler<T> dataConvertHandler;

    protected BeanListHandler<T> dataListConvertHandler;

    protected String selectSql;

    protected String selectAllSql;
    /**
     * 根据索引查找数据<br>
     * 少部分表会使用到
     */
    protected String selectByIndexSql;

    protected String saveSql;

    public BaseDao(Class<T> dataClazz) {
        super(dataClazz);
        init();
    }

    public BaseDao(Class<T> dataClazz, String tableName) {
        super(dataClazz, tableName);
        init();
    }

    protected void init() {
        initDbUtils();
        initSql();
    }

    protected void initDbUtils() {
        this.beanProcessor = new DefaultBeanProcessor<>(dataClazz);
        RowProcessor rowProcessor = new DefaultRowProcessor(beanProcessor);
        this.dataConvertHandler = new BeanHandler<>(dataClazz, rowProcessor);
        this.dataListConvertHandler = new BeanListHandler<>(dataClazz, rowProcessor);
    }

    protected void initSql() {
        selectSql = buildSelectSql();
        selectByIndexSql = buildSelectByIndexSql();
        selectAllSql = buildSelectAllSql();

        saveSql = buildSaveSql();
    }

    protected String buildSelectSql() {
        StringBuffer sb = new StringBuffer();
        sb.append("select * from `").append(tableName).append("` where `id`=?");
        return sb.toString();
    }

    protected String buildSelectByIndexSql() {
        return buildSelectSql();
    }

    protected String buildSelectAllSql() {
        StringBuffer sb = new StringBuffer();
        sb.append("select * from `").append(tableName).append("`");
        return sb.toString();
    }

    protected String buildSaveSql() {
        StringBuffer sb = new StringBuffer();
        sb.append("replace into `").append(tableName).append("` (");
        BeanFieldDescriptor[] fieldDescriptors = beanProcessor.getFieldDescriptors();
        boolean first = true;
        for (BeanFieldDescriptor fieldDescriptor : fieldDescriptors) {
            String name = fieldDescriptor.getColumnName();
            if (!first) {
                sb.append(',');
            }
            sb.append('`').append(name).append('`');
            first = false;
        }
        sb.append(") values(");
        for (int i = 0; i < fieldDescriptors.length - 1; i++) {
            sb.append("?,");
        }
        sb.append("?)");
        return sb.toString();
    }

    @Override
    public T select(Object key) throws Exception {
        long startTime = System.currentTimeMillis();
        QueryRunner queryRunner = getQueryRunner();
        try {
            T result = queryRunner.query(selectSql, dataConvertHandler, key);
            logCostTime(selectSql, startTime);
            return result;
        } catch (Exception e) {
            logger.error("execute sql[{}] error.", selectSql, e);
            throw e;
        }
    }

    @Override
    public List<T> selectAll() throws Exception {
        long startTime = System.currentTimeMillis();
        QueryRunner queryRunner = getQueryRunner();
        try {
            List<T> result = queryRunner.query(selectAllSql, dataListConvertHandler);
            logCostTime(selectAllSql, startTime);
            return result;
        } catch (Exception e) {
            logger.error("execute sql[{}] error.", selectAllSql, e);
            throw e;
        }
    }

    @Override
    public List<T> selectByIndexId(Object key) throws Exception {
        long startTime = System.currentTimeMillis();
        QueryRunner queryRunner = getQueryRunner();
        try {
            List<T> result = queryRunner.query(selectByIndexSql, dataListConvertHandler, key);
            logCostTime(selectByIndexSql, startTime);
            return result;
        } catch (Exception e) {
            logger.error("execute sql[{}] error.", selectByIndexSql, e);
            throw e;
        }
    }

    @Override
    public boolean save(T data) throws Exception {
        long startTime = System.currentTimeMillis();
        QueryRunner queryRunner = getQueryRunner();
        try {
            Object[] fieldValues = getFieldValues(data);
            int update = queryRunner.update(saveSql, fieldValues);
            logCostTime(saveSql, startTime);
            return update > 0;
        } catch (Exception e) {
            logger.error("execute sql[{}] error.", saveSql, e);
            throw e;
        }
    }

    @Override
    public void batchSave(Collection<T> dataList) throws Exception {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        QueryRunner queryRunner = getQueryRunner();
        for (T data : dataList) {
            long startTime = System.currentTimeMillis();
            try {
                Object[] fieldValues = getFieldValues(data);
                queryRunner.update(saveSql, fieldValues);
                logCostTime(saveSql, startTime);
            } catch (Exception e) {
                logger.error("execute sql[{}] error.", saveSql, e);
                throw e;
            }
        }
    }

    /**
     * 获取该数据相关的参数值
     * 
     * @param data
     * @return
     * @throws Exception
     */
    protected Object[] getFieldValues(T data) throws Exception {
        return beanProcessor.getFieldValues(data);
    }

    @Override
    public boolean delete(Object id) {
        throw new UnsupportedOperationException();
    }

}
