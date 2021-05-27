package org.jujubeframework.jdbc.binding.fmtmethod;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author John Li
 */
public class JoinMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        SimpleSequence sequence = (SimpleSequence) arguments.get(0);
        SimpleScalar str = (SimpleScalar) arguments.get(1);
        List list = sequence.toList();
        String separator = str.getAsString();
        if (!list.isEmpty()) {
            if (list.get(0) instanceof String) {
                StringBuilder result = new StringBuilder();
                for (Object obj : list) {
                    result.append("'").append(obj).append("'").append(separator);
                }
                return result.substring(0, result.length() - 1);
            } else {
                return StringUtils.join(list.toArray(), separator);
            }
        } else {
            return "";
        }
    }
}
