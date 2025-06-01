package gleam.persistence.register;

import javax.sql.DataSource;

import gleam.core.service.Context;
import gleam.core.service.ContextRegister;
import gleam.persistence.Dao;
import gleam.persistence.PersistenceData;
import gleam.persistence.Saver;
import gleam.persistence.SaverManager;
import gleam.persistence.dao.BaseDao;
import gleam.persistence.dbutils.JdbcManager;
import gleam.persistence.saver.DefaultSaver;
import gleam.persistence.saver.DelayConvertSaver;
import gleam.persistence.saver.converter.PersistenceDataConverter;

/**
 * 数据存储服务注册器<br>
 * 
 * @author hdh
 *
 */
public abstract class SaverRegister<T extends Context> implements ContextRegister<T> {

	@Override
	public void registerAll(T context) throws Exception {
		DataSource dataSource = buildDataSource();
		JdbcManager.getInstance().init(dataSource);
		context.registerService(SaverManager.getInstance());
		registerSavers();
	}

	/**
	 * 注册各类型的存储器
	 * 
	 * @throws Exception
	 */
	protected abstract void registerSavers() throws Exception;

	/**
	 * 创建数据源
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract DataSource buildDataSource() throws Exception;

	/**
	 * 根据数据类 创建默认的dao和saver进行注册
	 * 
	 * @param <D>
	 * @param dataClazz
	 */
	protected <D extends PersistenceData> void registerSaver(Class<D> dataClazz) {
		Dao<D> dao = new BaseDao<>(dataClazz);
		Saver<D> saver = new DefaultSaver<>(dao);
		SaverManager.getInstance().registerSaver(dataClazz, saver);
	}

	/**
	 * 根据dao和转化器 创建默认的转化器saver进行注册
	 * 
	 * @param <B>
	 * @param <D>
	 * @param dao
	 * @param converter
	 */
	protected <B, D extends PersistenceData> void registerSaver(Dao<D> dao, PersistenceDataConverter<B, D> converter) {
		Class<B> beanClazz = converter.getBeanClazz();
		Saver<B> saver = new DelayConvertSaver<>(dao, converter);
		SaverManager.getInstance().registerSaver(beanClazz, saver);
	}

	/**
	 * 根据dao 创建默认的saver并进行注册
	 * 
	 * @param <D>
	 * @param dao
	 */
	protected <D extends PersistenceData> void registerSaver(Dao<D> dao) {
		Class<D> dataClazz = dao.getDataClazz();
		Saver<D> saver = new DefaultSaver<>(dao);
		SaverManager.getInstance().registerSaver(dataClazz, saver);
	}

	/**
	 * 注册指定的saver
	 *
	 * @param <S>
	 * @param saver
	 */
	protected <S> void registerSaver(Saver<S> saver) {
		Class<S> beanClazz = saver.getBeanClazz();
		SaverManager.getInstance().registerSaver(beanClazz, saver);
	}
}
