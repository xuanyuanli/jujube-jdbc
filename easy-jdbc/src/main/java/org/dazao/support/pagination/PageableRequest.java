package org.dazao.support.pagination;

import java.io.Serializable;

/**
 * 分页请求
 * 
 * @author 李衡 Email：li15038043160@163.com
 * @since 2013-10-23 上午11:46:38
 */
public class PageableRequest implements Serializable {
    private static final long serialVersionUID = -590137694303783744L;
    private int index;
    private int size;
    private int start;

    /**
     * 总条数。放置这个元素的目的是，如果在第一页查询出了totalElements，那么后面的页数中，就可以直接使用totalElements。
     * 而不用再次查询总条数
     */
    private long totalElements;

    public PageableRequest(int index, int size) {
        super();
        setIndex(index);
        setSize(size);
    }

    public PageableRequest() {
        super();
        this.index = 1;
        this.size = Pageable.DEFAULT_SIZE;
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
        return size < 1 ? Pageable.DEFAULT_SIZE : size;
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

    public <T> Pageable<T> newPageable() {
        return new Pageable<T>(index, size, start);
    }

    /**
     * 构建分页请求
     * 
     * @since 2013-10-23 下午3:16:14
     * @author 李衡 Email：li15038043160@163.com
     * @param pageRequest
     * @return
     */
    public static PageableRequest buildPageRequest(PageableRequest pageRequest) {
        if (pageRequest == null) {
            pageRequest = new PageableRequest();
        }
        if (pageRequest.getIndex() < 1) {
            pageRequest.setIndex(1);
        }
        if (pageRequest.getSize() < 1) {
            pageRequest.setSize(Pageable.DEFAULT_SIZE);
        }
        return pageRequest;
    }

    public static PageableRequest buildPageRequest() {
        return buildPageRequest(null);
    }
}
