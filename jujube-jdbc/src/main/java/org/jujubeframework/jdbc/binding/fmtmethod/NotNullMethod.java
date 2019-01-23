package org.jujubeframework.jdbc.binding.fmtmethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author John Li
 */
public class NotNullMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if(arguments==null || arguments.isEmpty() || arguments.get(0)==null) {
            return  false;
        }
        return arguments.get(0).toString() != null;
    }
}
