package org.jujubeframework.jdbc.binding.fmtmethod;

import freemarker.template.TemplateMethodModelEx;

import java.util.List;

/**
 * @author John Li
 */
public class NotNullMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) {
        if(arguments==null || arguments.isEmpty() || arguments.get(0)==null) {
            return  false;
        }
        return arguments.get(0).toString() != null;
    }
}
