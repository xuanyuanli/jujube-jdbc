package org.dazao.support.pagination;

import org.jujubeframework.lang.Record;

/**
 * 分页列表搜索规则
 *
 * @author John Li
 */
public class SearchSpec {
    private Record simpleSpec = new Record();
    private Record complexSpec = new Record();

    public void putSimple(String key, Object value) {
        simpleSpec.put(key, value);
    }

    public void putComplex(String key, Object value) {
        complexSpec.put(key, value);
    }

    public Record getSimpleSpec() {
        return simpleSpec;
    }

    public Record getComplexSpec() {
        return complexSpec;
    }

}
