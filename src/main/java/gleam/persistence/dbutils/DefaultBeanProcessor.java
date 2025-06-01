package gleam.persistence.dbutils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.lang3.StringUtils;

import gleam.persistence.dbutils.type.JdbcBeanTypeHandler;
import gleam.persistence.dbutils.type.JdbcTypeHandler;
import gleam.util.ClazzUtil;
import gleam.util.StringUtil;

/**
 * 将{@link ResultSet}转为java中的数据结构的处理类<br>
 * {@link BeanProcessor}难以拓展 重写大部分方法<br>
 * 核心方法<br>
 * {@link #toBean(ResultSet, Class)}<br>
 * {@link #toBeanList(ResultSet, Class)}
 * 
 * @author hdh
 *
 * @param <T>
 */
public class DefaultBeanProcessor<T> {
    /**
     * Special array value used by <code>mapColumnsToProperties</code> that
     * indicates there is no bean property that matches a column from a
     * <code>ResultSet</code>.
     */
    protected static final int PROPERTY_NOT_FOUND = -1;

    private final Class<T> dataClazz;

    private final BeanFieldDescriptor[] fieldDescriptors;

    public DefaultBeanProcessor(Class<T> dataClazz) {
        super();
        this.dataClazz = dataClazz;
        this.fieldDescriptors = buildFieldDescriptors(dataClazz);
    }

    public Class<T> getDataClazz() {
        return dataClazz;
    }

    /**
     * 获取并创建该数据类的参数描述列表
     * 
     * @param dataClazz
     * @return
     * @throws Exception
     */
    private BeanFieldDescriptor[] buildFieldDescriptors(Class<?> dataClazz) {
        List<Field> beanFields = ClazzUtil.getBeanFields(dataClazz);
        BeanFieldDescriptor[] fieldDescriptors = new BeanFieldDescriptor[beanFields.size()];
        for (int i = 0; i < beanFields.size(); i++) {
            Field field = beanFields.get(i);
            Method readMethod = ClazzUtil.getReadMethod(dataClazz, field);
            Method writeMethod = ClazzUtil.getWriteMethod(dataClazz, field);
            Type fieldType = field.getGenericType();
            String fieldName = field.getName();
            String columnName = StringUtil.camel2UnderscoreLower(fieldName);

            BeanFieldDescriptor fieldDescriptor = new BeanFieldDescriptor();
            fieldDescriptor.setFieldType(fieldType);
            fieldDescriptor.setFieldName(fieldName);
            fieldDescriptor.setColumnName(columnName);
            fieldDescriptor.setReadMethod(readMethod);
            fieldDescriptor.setWriteMethod(writeMethod);
            fieldDescriptors[i] = fieldDescriptor;
        }
        return fieldDescriptors;
    }

    public T toBean(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int[] columnToProperty = this.mapColumnsToProperties(rsmd);
        T bean = parseBean(rs, columnToProperty);
        return bean;
    }

    public List<T> toBeanList(ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<T>();
        if (!rs.next()) {
            return results;
        }
        ResultSetMetaData rsmd = rs.getMetaData();
        int[] columnToProperty = this.mapColumnsToProperties(rsmd);
        do {
            T bean = parseBean(rs, columnToProperty);
            results.add(bean);
        } while (rs.next());
        return results;
    }

    /**
     * 获取该数据需要保存的各参数
     * 
     * @param bean
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object[] getFieldValues(T bean) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object[] result = new Object[fieldDescriptors.length];
        JdbcManager jdbcManager = JdbcManager.getInstance();
        for (int i = 0; i < fieldDescriptors.length; i++) {
            BeanFieldDescriptor fieldDescriptor = fieldDescriptors[i];
            Method readMethod = fieldDescriptor.getReadMethod();
            Object value = readMethod.invoke(bean);
            Type fieldType = fieldDescriptor.getFieldType();
            JdbcBeanTypeHandler typeHandler = jdbcManager.getBeanTypeHandler(fieldType);
            if (typeHandler != null) {
                value = typeHandler.convertParam(value);
            }
            result[i] = value;
        }
        return result;
    }

    protected T newInstance() throws SQLException {
        try {
            return dataClazz.getDeclaredConstructor().newInstance();
        } catch (final IllegalAccessException | InstantiationException | InvocationTargetException |
                NoSuchMethodException e) {
            throw new SQLException("Cannot create " + dataClazz.getName() + ": " + e.getMessage());
        }
    }

    /**
     * 获取各参数在sql返回的结果集中对应的下标<br>
     * 从1开始
     * 
     * @param rsmd
     * @return
     * @throws SQLException
     */
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd) throws SQLException {

        int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);
        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }
            columnToProperty[col] = getFieldDescriptorIndex(columnName);
        }
        return columnToProperty;
    }

    private int getFieldDescriptorIndex(String columnName) {
        for (int i = 0; i < fieldDescriptors.length; i++) {
            BeanFieldDescriptor fieldDescriptor = fieldDescriptors[i];
            if (StringUtils.equals(fieldDescriptor.getColumnName(), columnName)) {
                return i;
            }
        }
        for (int i = 0; i < fieldDescriptors.length; i++) {
            BeanFieldDescriptor fieldDescriptor = fieldDescriptors[i];
            if (StringUtils.equals(fieldDescriptor.getFieldName(), columnName)) {
                return i;
            }
        }
        return PROPERTY_NOT_FOUND;

    }

    private T parseBean(ResultSet rs, int[] columnToProperty) throws SQLException {
        T bean = newInstance();
        JdbcManager jdbcManager = JdbcManager.getInstance();
        for (int i = 1; i < columnToProperty.length; i++) {

            if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
                continue;
            }

            BeanFieldDescriptor fieldDescriptor = fieldDescriptors[columnToProperty[i]];
            Type fieldType = fieldDescriptor.getFieldType();

            JdbcTypeHandler typeHandler = jdbcManager.getTypeHandler(fieldType);
            if (typeHandler == null) {
                int columnType = rs.getMetaData().getColumnType(i);
                throw new SQLException("parse bean [" + dataClazz.getName() + "] field[" + fieldDescriptor.getFieldName() + "] fieldType["
                        + fieldType.getTypeName() + "] columnType[" + columnType + "] error.JdbcTypeHandler cant find.");
            }
            Object value = typeHandler.getResult(rs, i);
            this.callSetter(bean, fieldDescriptor, value);
        }
        return bean;
    }

    /**
     * Calls the setter method on the target object for the given property. If no
     * setter method exists for the property, this method does nothing.
     * 
     * @param target The object to set the property on.
     * @param prop   The property to set.
     * @param value  The value to pass into the setter.
     * @throws SQLException if an error occurs setting the property.
     */
    private void callSetter(T target, BeanFieldDescriptor prop, Object value) throws SQLException {
        final Method setter = prop.getWriteMethod();
        if (setter == null || setter.getParameterTypes().length != 1) {
            return;
        }
        try {
            final Class<?> firstParamClazz = setter.getParameterTypes()[0];
            // Don't call setter if the value object isn't the right type
            if (!this.isCompatibleType(value, firstParamClazz)) {
                throw new SQLException("Cannot set " + prop.getFieldName() + ": incompatible types, cannot convert " + value.getClass().getName() + " to "
                        + firstParamClazz.getName());
                // value cannot be null here because isCompatibleType allows null
            }
            setter.invoke(target, value);
        } catch (final IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new SQLException("Cannot set " + prop.getFieldName() + ": " + e.getMessage());
        }
    }

    /**
     * ResultSet.getObject() returns an Integer object for an INT column. The setter
     * method for the property might take an Integer or a primitive int. This method
     * returns true if the value can be successfully passed into the setter method.
     * Remember, Method.invoke() handles the unwrapping of Integer into an int.
     *
     * @param value The value to be passed into the setter method.
     * @param type  The setter's parameter type (non-null)
     * @return boolean True if the value is compatible (null => true)
     */
    private boolean isCompatibleType(final Object value, final Class<?> type) {
        // Do object check first, then primitives
        if (value == null || type.isInstance(value) || matchesPrimitive(type, value.getClass())) {
            return true;
        }
        return false;
    }

    /**
     * Check whether a value is of the same primitive type as {@code targetType}.
     *
     * @param targetType The primitive type to target.
     * @param valueType  The value to match to the primitive type.
     * @return Whether {@code valueType} can be coerced (e.g. autoboxed) into
     *         {@code targetType}.
     */
    private boolean matchesPrimitive(final Class<?> targetType, final Class<?> valueType) {
        if (!targetType.isPrimitive()) {
            return false;
        }
        try {
            // see if there is a "TYPE" field. This is present for primitive wrappers.
            final Field typeField = valueType.getField("TYPE");
            final Object primitiveValueType = typeField.get(valueType);

            if (targetType == primitiveValueType) {
                return true;
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            // an inaccessible TYPE field is a good sign that we're not working with a
            // primitive wrapper.
            // nothing to do. we can't match for compatibility
        }
        return false;
    }

    public BeanFieldDescriptor[] getFieldDescriptors() {
        return fieldDescriptors;
    }

}
