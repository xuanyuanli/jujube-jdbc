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
        SimpleSequence sequence = (SimpleSequence)arguments.get(0);
        SimpleScalar str = (SimpleScalar)arguments.get(1);
        return StringUtils.join(sequence.toList().toArray(),str.getAsString());
    }
}
