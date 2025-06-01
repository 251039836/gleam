package gleam.persistence.dbutils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

import gleam.exception.RepeatRegisterException;
import gleam.persistence.dbutils.type.JdbcBeanTypeHandler;
import gleam.persistence.dbutils.type.JdbcTypeHandler;
import gleam.persistence.dbutils.type.base.BooleanTypeHandler;
import gleam.persistence.dbutils.type.base.ByteArrayTypeHandler;
import gleam.persistence.dbutils.type.base.ByteTypeHandler;
import gleam.persistence.dbutils.type.base.DoubleTypeHandler;
import gleam.persistence.dbutils.type.base.FloatTypeHandler;
import gleam.persistence.dbutils.type.base.IntTypeHandler;
import gleam.persistence.dbutils.type.base.LongTypeHandler;
import gleam.persistence.dbutils.type.base.ShortTypeHandler;
import gleam.persistence.dbutils.type.base.StringTypeHandler;
import gleam.util.ClazzUtil;

/**
 * jdbc管理类<br>
 * hikariCp+dbutils魔改版实现
 * 
 * @author hdh
 *
 */
public class JdbcManager {
	private static JdbcManager instance = new JdbcManager();

	public static JdbcManager getInstance() {
		return instance;
	}

	private final Map<Type, JdbcTypeHandler> allTypeHandlers = new HashMap<>();

	private final Map<Type, JdbcBeanTypeHandler<?>> customTypeHandlers = new HashMap<>();

	private QueryRunner queryRunner;

	public void init(DataSource dataSource) throws Exception {
		registerBaseHandlers();
		registerCustomBeanHandlers();

		queryRunner = new QueryRunner(dataSource);
	}

	@SuppressWarnings("rawtypes")
	private void registerCustomBeanHandlers() throws Exception {
		List<JdbcBeanTypeHandler> typeHandlers = ClazzUtil.scanImplAndNewInstances(ClazzUtil.GAME_PACKAGE_NAME,
				JdbcBeanTypeHandler.class);
		for (JdbcBeanTypeHandler typeHandler : typeHandlers) {
			registerTypeHandler(typeHandler);
		}
	}

	private void registerBaseHandlers() {
		BooleanTypeHandler booleanHandler = new BooleanTypeHandler();
		allTypeHandlers.put(boolean.class, booleanHandler);
		allTypeHandlers.put(Boolean.class, booleanHandler);

		ByteTypeHandler byteHandler = new ByteTypeHandler();
		allTypeHandlers.put(byte.class, byteHandler);
		allTypeHandlers.put(Byte.class, byteHandler);

		ShortTypeHandler shortHandler = new ShortTypeHandler();
		allTypeHandlers.put(short.class, shortHandler);
		allTypeHandlers.put(Short.class, shortHandler);

		IntTypeHandler intHandler = new IntTypeHandler();
		allTypeHandlers.put(int.class, intHandler);
		allTypeHandlers.put(Integer.class, intHandler);

		LongTypeHandler longHandler = new LongTypeHandler();
		allTypeHandlers.put(long.class, longHandler);
		allTypeHandlers.put(Long.class, longHandler);

		FloatTypeHandler floatHandler = new FloatTypeHandler();
		allTypeHandlers.put(float.class, floatHandler);
		allTypeHandlers.put(Float.class, floatHandler);

		DoubleTypeHandler doubleHandler = new DoubleTypeHandler();
		allTypeHandlers.put(double.class, doubleHandler);
		allTypeHandlers.put(Double.class, doubleHandler);

		allTypeHandlers.put(String.class, new StringTypeHandler());
		allTypeHandlers.put(byte[].class, new ByteArrayTypeHandler());
	}

	private void registerTypeHandler(JdbcBeanTypeHandler<?> typeHandler) {
		Type type = typeHandler.getType();
		JdbcTypeHandler oldTypeHandler = allTypeHandlers.put(type, typeHandler);
		if (oldTypeHandler != null) {
			throw new RepeatRegisterException("registerJdbcTypeHandler error.type[" + type.getTypeName() + "] handler1["
					+ typeHandler.getClass().getName() + "] handler2[" + typeHandler.getClass().getName() + "]");
		}
		customTypeHandlers.put(type, typeHandler);
	}

	public QueryRunner getQueryRunner() {
		return queryRunner;
	}

	public void setQueryRunner(QueryRunner queryRunner) {
		this.queryRunner = queryRunner;
	}

	public Map<Type, JdbcTypeHandler> getAllTypeHandlers() {
		return allTypeHandlers;
	}

	public Map<Type, JdbcBeanTypeHandler<?>> getCustomTypeHandlers() {
		return customTypeHandlers;
	}

	public JdbcTypeHandler getTypeHandler(Type type) {
		return allTypeHandlers.get(type);
	}

	public JdbcBeanTypeHandler<?> getBeanTypeHandler(Type type) {
		return customTypeHandlers.get(type);
	}

}
