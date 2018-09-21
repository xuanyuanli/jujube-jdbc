package org.dazao.persistence.base.datasource;

/**
 * 动态数据源
 *
 * @author John Li
 */
public class DynamicDataSourceHolder {

    public static final ThreadLocal<DataSourceType> HOLDER = new ThreadLocal<>();

    /**
     * 标记为写数据源
     */
    static void markWrite() {
        HOLDER.set(DataSourceType.WRITE);
    }

    /**
     * 标记为读数据源
     */
    static void markRead() {
        HOLDER.set(DataSourceType.READ);
    }

    /**
     * 重置
     */
    static void reset() {
        HOLDER.remove();
    }

    /**
     * 是否还未设置数据源
     */
    static boolean isChoiceNone() {
        return null == HOLDER.get();
    }

    /**
     * 当前是否选择了写数据源
     */
    static boolean isChoiceWrite() {
        return DataSourceType.WRITE == HOLDER.get();
    }

    /**
     * 当前是否选择了读数据源
     */
    static boolean isChoiceRead() {
        return DataSourceType.READ == HOLDER.get();
    }

}
