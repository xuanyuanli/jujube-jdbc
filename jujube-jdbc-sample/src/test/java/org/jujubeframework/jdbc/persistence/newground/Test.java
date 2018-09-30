package org.jujubeframework.jdbc.persistence.newground;


import com.google.common.collect.Lists;
import org.jujubeframework.util.Ftls;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        String freemarkerSql = "select * from `user` u left join  `department` d on u.department_id=d.id\n" + "where 1=1\n" + "<#if (name?? && name!='')>\n" + "  and u.name like '%${name}%'\n" + "</#if>\n" + "<#if age gt 0>\n" + "  and u.age > ?\n" + "</#if>\n" + "<#if ids??>\n" + "  and id in (<#list ids as x>${x}<#if x_has_next>,</#if></#list>)\n" + "</#if>\n" + "<#if (nameDesc?? && nameDesc!='')>\n" + "  order by name desc\n" + "</#if>";
        Map<String,Object> map = new HashMap<>();
        map.put("name","abc");
        map.put("age",10);
        map.put("ids", Lists.newArrayList(1,2,3));
        System.out.println(Ftls.processStringTemplateToString(freemarkerSql,map));
    }

}
