package org.jujubeframework.jdbc.persistence.base.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态数据源
 *
 * @author John Li
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private DataSource writeDataSource;
    private List<DataSource> readDataSources;
    private int readDataSourceSize = 0;

    private AtomicInteger readIndex = new AtomicInteger(0);

    /**
     * 数据源键名
     */
    private static final String DATASOURCE_KEY_WRITE = DataSourceType.WRITE.name();
    private static final String DATASOURCE_KEY_READ = DataSourceType.READ.name();

    @Override
    public void afterPropertiesSet() {
        if (this.writeDataSource == null) {
            throw new IllegalArgumentException("Property 'writeDataSource' is required");
        }
        setDefaultTargetDataSource(writeDataSource);
        Map<Object, Object> targetDataSources = new HashMap<>(2);
        targetDataSources.put(DATASOURCE_KEY_WRITE, writeDataSource);
        if (this.readDataSources == null) {
            readDataSourceSize = 0;
        } else {
            for (int i = 0; i < readDataSources.size(); i++) {
                DataSource readDataSource = readDataSources.get(i);
                targetDataSources.put(DATASOURCE_KEY_READ + i, readDataSource);
            }
            readDataSourceSize = readDataSources.size();
        }
        setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        try {
            if (DynamicDataSourceHolder.isChoiceWrite() || readDataSourceSize == 0) {
                return DATASOURCE_KEY_WRITE;
            }
            int index = readIndex.incrementAndGet() % readDataSourceSize;
            return DATASOURCE_KEY_READ + index;
        } finally {
            DynamicDataSourceHolder.reset();
        }
    }

    public DataSource getWriteDataSource() {
        return writeDataSource;
    }

    public void setWriteDataSource(DataSource writeDataSource) {
        this.writeDataSource = writeDataSource;
    }

    public List<DataSource> getReadDataSources() {
        return readDataSources;
    }

    public void setReadDataSources(List<DataSource> readDataSources) {
        this.readDataSources = readDataSources;
    }

}
