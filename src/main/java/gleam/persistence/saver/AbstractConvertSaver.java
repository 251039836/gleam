package gleam.persistence.saver;

import java.util.List;

import gleam.persistence.Changeable;
import gleam.persistence.Dao;
import gleam.persistence.PersistenceData;
import gleam.persistence.saver.converter.PersistenceDataConverter;

public abstract class AbstractConvertSaver<B, D extends PersistenceData> extends AbstractSaver<B> {

    protected final Dao<D> dao;

    protected final PersistenceDataConverter<B, D> converter;

    public AbstractConvertSaver(long tickInterval, Dao<D> dao, PersistenceDataConverter<B, D> converter) {
        super(tickInterval);
        if (dao == null) {
            throw new IllegalArgumentException("dao is null.");
        }
        if (converter == null) {
            throw new IllegalArgumentException("converter is null.");
        }
        this.dao = dao;
        this.converter = converter;
    }

    @Override
    public boolean blockSave(B bean) throws Exception {
        if (bean == null) {
            throw new NullPointerException("blockSave error.bean is null.");
        }
        D data = converter.bean2Data(bean);
        if (data == null) {
            throw new NullPointerException("blockSave error.bean2Data fail.");
        }
        boolean result = dao.save(data);
        if (result && data instanceof Changeable) {
            Changeable changeData = (Changeable) data;
            changeData.setChange(false);
        }
        return result;
    }

    @Override
    public Class<B> getBeanClazz() {
        return converter.getBeanClazz();
    }

    public PersistenceDataConverter<B, D> getConverter() {
        return converter;
    }

    public Dao<D> getDao() {
        return dao;
    }

    @Override
    public List<B> loadAll() throws Exception {
        List<D> datas = dao.selectAll();
        return converter.datas2Beans(datas);
    }

    @Override
    public List<B> loadListByIndexId(Object key) throws Exception {
        if (key == null) {
            throw new NullPointerException("load bean error.key is null.");
        }
        List<D> datas = dao.selectByIndexId(key);
        return converter.datas2Beans(datas);
    }
}
