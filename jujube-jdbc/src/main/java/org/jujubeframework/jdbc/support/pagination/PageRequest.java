package org.jujubeframework.jdbc.support.pagination;

import java.io.Serializable;

/**
 * 分页请求
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -590137694303783744L;
    private int index;
    private int size;
    private int start;

    /**
     * 总条数。放置这个元素的目的是，如果在第一页查询出了totalElements，那么后面的页数中，就可以直接使用totalElements。
     * 而不用再次查询总条数
     */
    private long totalElements;

    public PageRequest(int index, int size) {
        super();
        setIndex(index);
        setSize(size);
    }

    public PageRequest() {
        super();
        this.index = 1;
        this.size = Page.DEFAULT_SIZE;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (index <= 0) {
            index = 1;
        }
        this.index = index;
    }

    public int getSize() {
        return size < 1 ? Page.DEFAULT_SIZE : size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getStart() {
        return start;
    }

    public <T> Page<T> newPageable() {
        return new Page<T>(index, size, start);
    }

    /**
     * 构建分页请求
     *
     * @param pageRequest
     * @return
     * @author John Li Email：jujubeframework@163.com
     */
    public static PageRequest buildPageRequest(PageRequest pageRequest) {
        if (pageRequest == null) {
            pageRequest = new PageRequest();
        }
        if (pageRequest.getIndex() < 1) {
            pageRequest.setIndex(1);
        }
        if (pageRequest.getSize() < 1) {
            pageRequest.setSize(Page.DEFAULT_SIZE);
        }
        return pageRequest;
    }

    public static PageRequest buildPageRequest() {
        return buildPageRequest(null);
    }
}
