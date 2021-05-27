package org.jujubeframework.jdbc.binding.fmtmethod;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.utility.ClassUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 类型判断,返回string、number
 *
 * @author John Li
 */
public class TypeOfMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List arguments) {
        if (arguments == null || arguments.isEmpty() || arguments.get(0) == null) {
            return null;
        }
        TemplateModel obj = (TemplateModel) arguments.get(0);
        return StringUtils.splitByWholeSeparator(ClassUtil.getFTLTypeDescription(obj), " (")[0];
    }
}
