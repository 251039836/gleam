package gleam.persistence.saver.converter;

import java.util.Collection;
import java.util.List;

import gleam.persistence.PersistenceData;

public interface PersistenceDataConverter<B, D extends PersistenceData> {
    /**
     * 获取该bean的唯一主键<br>
     * 对应{@link PersistenceData#getPrimaryKey()}
     * 
     * @param bean
     * @return
     */
    Object getKey(B bean);

    D bean2Data(B bean);

    List<D> beans2Datas(Collection<B> beans);

    B data2Bean(D data) throws Exception;

    List<B> datas2Beans(Collection<D> datas) throws Exception;

    Class<B> getBeanClazz();

    Class<D> getDataClazz();

}
