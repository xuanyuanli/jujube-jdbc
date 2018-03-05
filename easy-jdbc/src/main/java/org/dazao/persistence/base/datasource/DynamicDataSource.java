package org.dazao.persistence.base.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/** 动态数据源 */
public class DynamicDataSource extends AbstractRoutingDataSource {

	private DataSource writeDataSource;
	private List<DataSource> readDataSources;
	private int readDataSourceSize = 0;

	private AtomicInteger readIndex = new AtomicInteger(0);

	/**
	 * 数据源键名
	 */
	private static final String DATASOURCE_KEY_WRITE = DataSourceType.write.name();
	private static final String DATASOURCE_KEY_READ = DataSourceType.read.name();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource#
	 * afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.writeDataSource == null) {
			throw new IllegalArgumentException("Property 'writeDataSource' is required");
		}
		setDefaultTargetDataSource(writeDataSource);
		Map<Object, Object> targetDataSources = new HashMap<>();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource#
	 * determineCurrentLookupKey()
	 */
	@Override
	protected Object determineCurrentLookupKey() {
		if (DynamicDataSourceHolder.isChoiceWrite() || readDataSourceSize == 0) {
			return DATASOURCE_KEY_WRITE;
		}
		int index = readIndex.incrementAndGet() % readDataSourceSize;
		return DATASOURCE_KEY_READ + index;
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
