package org.jujubeframework.jdbc.base;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * sql查询后置处理器
 *
 * @author John Li
 */
public interface SqlQueryPostHandler {
    SqlQuery postHandle(String sql, Object[] params);

    @Data
    @AllArgsConstructor
    class SqlQuery {
        private String sql;
        private Object[] params;
    }
}
