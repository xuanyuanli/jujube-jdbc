package org.jujubeframework.jdbc.support.pagination;

import org.jujubeframework.jdbc.util.DataTypeConvertor;
import org.jujubeframework.lang.Record;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * 分页中间类
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class Pageable<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = -566814709144497590L;

    /**
     * 数据
     */
    private List<T> data;
    /**
     * 总条数
     */
    private long totalElements;
    /**
     * 每页显示多少条
     */
    private int size;
    /**
     * 当前页码
     */
    private int index;
    /**
     * 相当于limit begin（用于自定义，一般来说用不到）
     */
    private int start;
    /**
     * 默认每页显示条数
     */
    public static final int DEFAULT_SIZE = 10;

    public Pageable(int index, int size, int start) {
        super();
        this.size = size;
        this.index = index;
        this.start = start;
    }

    public Pageable(int index, int size) {
        super();
        this.size = size;
        this.index = index;
    }

    public Pageable(int index) {
        super();
        this.index = index;
        this.size = DEFAULT_SIZE;
    }

    public Pageable() {
        super();
        this.index = 1;
        this.size = DEFAULT_SIZE;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 获得总共多少页
     */
    public long getTotalPages() {
        return (totalElements + size - 1) / size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getSize() {
        return size < 1 ? DEFAULT_SIZE : size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStart() {
        if (start <= 0) {
            return (index - 1) * size;
        } else {
            return start;
        }
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 是否有前一页
     */
    public boolean hasPreviousPage() {
        return index > 1 ? true : false;
    }

    /**
     * 是否是第一页
     */
    public boolean isFirstPage() {
        return !hasPreviousPage();
    }

    /**
     * 是否 有后一页
     */
    public boolean hasNextPage() {
        return index < getTotalPages() ? true : false;
    }

    /**
     * 是否为最后一页
     */
    public boolean isLastPage() {
        return !hasNextPage();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    /**
     * 页码分页组件条用到，获取组件条开始的数字
     */
    public int getBarBegin(int barSize) {
        int current = getIndex();
        int begin = Math.max(1, current - barSize / 2);
        int end = Math.min(begin + (barSize - 1), (int) getTotalPages());
        if (end - begin < barSize) {
            begin = Math.max(1, end - (barSize - 1));
        }
        return begin;
    }

    /**
     * 页码分页组件条用到，获取组件条结束的数字
     */
    public int getBarEnd(int barSize) {
        int current = getIndex();
        int begin = Math.max(1, current - barSize / 2);
        int end = Math.min(begin + (barSize - 1), (int) getTotalPages());
        return end;
    }

    /**
     * 把泛型为Record的Pageable转换为对应类型的Pageable
     */
    @SuppressWarnings("hiding")
    public <T extends Serializable> Pageable<T> toGenericType(Class<T> cl) {
        @SuppressWarnings("unchecked") Pageable<Record> page = (Pageable<Record>) this;
        return DataTypeConvertor.convertPageableGenericType(page, cl);
    }
}
