package gleam.persistence.saver.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.persistence.PersistenceData;

public abstract class AbstractDataConverter<B, D extends PersistenceData> implements PersistenceDataConverter<B, D> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<D> beans2Datas(Collection<B> beans) {
        if (beans == null || beans.isEmpty()) {
            return new ArrayList<>();
        }
        List<D> dataList = new ArrayList<>(beans.size());
        for (B bean : beans) {
            D data = bean2Data(bean);
            if (data == null) {
                logger.warn("bean[{}] convert data fail.", bean);
                continue;
            }
            dataList.add(data);
        }
        return dataList;
    }

    @Override
    public List<B> datas2Beans(Collection<D> datas) throws Exception {
        if (datas == null || datas.isEmpty()) {
            return new ArrayList<>();
        }
        List<B> beanList = new ArrayList<>(datas.size());
        for (D data : datas) {
            B bean = data2Bean(data);
            if (bean == null) {
                logger.warn("data key[{}] convert bean fail.data:{}", data.getPrimaryKey(), data);
                continue;
            }
            beanList.add(bean);
        }
        return beanList;
    }
}
