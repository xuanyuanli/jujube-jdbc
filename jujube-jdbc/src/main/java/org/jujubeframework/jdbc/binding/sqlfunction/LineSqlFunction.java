package org.jujubeframework.jdbc.binding.sqlfunction;

import org.jujubeframework.util.Texts;

/**
 * @author John Li
 */
public interface LineSqlFunction extends SqlFunction {
    default String getFunctionParam(String function) {
        String reg = "\\(('|\")(.*?)\\1\\)";
        String group = "";
        String[] groups = Texts.getGroups(reg, function);
        int three = 3;
        if (groups.length == three) {
            group = groups[2];
        }
        return group;
    }
}
