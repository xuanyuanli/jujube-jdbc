package org.dazao.persistence.base.datasource;

/** 动态数据源 */
public class DynamicDataSourceHolder {

    public static final ThreadLocal<DataSourceType> holder = new ThreadLocal<>();

    /**
     * 标记为写数据源
     */
    static void markWrite() {
        holder.set(DataSourceType.write);
    }

    /**
     * 标记为读数据源
     */
    static void markRead() {
        holder.set(DataSourceType.read);
    }

    /**
     * 重置
     */
    static void reset() {
        holder.set(null);
    }

    /**
     * 是否还未设置数据源
     */
    static boolean isChoiceNone() {
        return null == holder.get();
    }

    /**
     * 当前是否选择了写数据源
     */
    static boolean isChoiceWrite() {
        return DataSourceType.write == holder.get();
    }

    /**
     * 当前是否选择了读数据源
     */
    static boolean isChoiceRead() {
        return DataSourceType.read == holder.get();
    }

}
