package org.dazao.support.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对于非静态方法的日志支持
 *
 * @author 李衡 Email：li15038043160@163.com
 * @since 2014年8月26日 下午3:30:49
 */
public abstract class Logable {
    protected Logger logger = LoggerFactory.getLogger(getClass());
}
