package gleam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * 反射工具类
 * @author redback
 * @version 1.00
 * @time 2021-11-3 15:58
 */
public class ReflectionUtil {

    protected static final Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);


    final static Map<Class<?>, Map<String, Field>> classFieldMap = new ConcurrentHashMap<>();



    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     *
     * @param object Object
     * @param fieldName FieldName
     * @return object
     */
    public static Object getFieldValue(Object object, String fieldName) {
        return getFieldValue(object, fieldName, false);
    }

    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     *
     * @param object Object
     * @param fieldName FieldName
     * @return Object
     */
    public static <T> Object getFieldValue(Object object, String fieldName, boolean cache) {

        Field field;

        if (cache){
            Class<?> clazz = object.getClass();
            Map<String, Field> fieldMap = classFieldMap.computeIfAbsent(clazz, (k) -> {
                Map<String, Field> fieldCache = new HashMap<>();
                List<Field> fields = getFieldList(clazz);
                for (Field f : fields) {
                    fieldCache.put(f.getName(), f);
                }
                return fieldCache;
            });
            field = fieldMap.get(fieldName);
        }else{
            field = getDeclaredField(object, fieldName);
        }

        if (field == null) {
            return null;
        }

        makeAccessible(field);

        Object result = null;

        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            logger.error("getFieldValue:", e);
        }

        return result;
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     *
     * @param object Object
     * @param fieldName FieldName
     * @param value Value
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        setFieldValue(object, fieldName, value, false);
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     *
     * @param object object
     * @param fieldName fileName
     * @param value setValue
     */
    public static void setFieldValue(Object object, String fieldName, Object value, boolean cache) {
        Field field;

        if (cache){
            Class<?> clazz = object.getClass();
            Map<String, Field> fieldMap = classFieldMap.computeIfAbsent(clazz, (k) -> {
                Map<String, Field> fieldCache = new HashMap<>();
                List<Field> fields = getFieldList(clazz);
                for (Field f : fields) {
                    fieldCache.put(f.getName(), f);
                }
                return fieldCache;
            });
            field = fieldMap.get(fieldName);
        }else{
            field = getDeclaredField(object, fieldName);
        }

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }
        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            logger.error("setFieldValue:", e);
        }
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param object object
     * @param filedName fileName
     * @return Field
     */
    public static Field getDeclaredField(Object object, String filedName) {

        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(filedName);
            } catch (NoSuchFieldException e) {
                //Field 不在当前类定义, 继续向上转型
            }
        }
        return null;
    }

    /**
     * 使 filed 变为可访问
     *
     * @param field Field
     */
    public static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    public static List<Field> getFieldList(Class<?> clazz) {
        if (clazz.getSuperclass() != null) {
            /* 排除重载属性 */
            Map<String, Field> fieldMap = excludeOverrideSuperField(clazz.getDeclaredFields(),
                    /* 处理父类字段 */
                    getFieldList(clazz.getSuperclass()));
            List<Field> fieldList = new ArrayList<>();
            /*
             * 重写父类属性过滤后处理忽略部分，支持过滤父类属性功能
             * 场景：中间表不需要记录创建时间，忽略父类 createTime 公共属性
             * 中间表实体重写父类属性 ` private transient Date createTime; `
             */
            fieldMap.forEach((k, v) -> {
                /* 过滤静态属性 */
                if (!Modifier.isStatic(v.getModifiers())
                        /* 过滤 transient关键字修饰的属性 */
                        && !Modifier.isTransient(v.getModifiers())) {
                    fieldList.add(v);
                }
            });
            return fieldList;
        } else {
            return Collections.emptyList();
        }
    }

    private static Map<String, Field> excludeOverrideSuperField(Field[] fields, List<Field> superFieldList) {
        // 子类属性
        Map<String, Field> fieldMap = Stream.of(fields).collect(toMap(Field::getName, identity(),
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new));
        superFieldList.stream().filter(field -> !fieldMap.containsKey(field.getName()))
                .forEach(f -> fieldMap.put(f.getName(), f));
        return fieldMap;
    }

}
