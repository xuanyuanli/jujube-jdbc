package org.jujubeframework.jdbc.binding.fmtmethod;

import freemarker.template.TemplateMethodModelEx;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author John Li
 */
public class NotBlankMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) {
        if(arguments==null || arguments.isEmpty() || arguments.get(0)==null) {
            return  false;
        }
        return StringUtils.isNotBlank(arguments.get(0).toString());
    }
}
