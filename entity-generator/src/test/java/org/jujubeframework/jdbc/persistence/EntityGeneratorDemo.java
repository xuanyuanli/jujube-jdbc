package org.jujubeframework.jdbc.persistence;

import org.jujubeframework.jdbc.generator.EntityGenerator;
import org.jujubeframework.util.Utils;

public class EntityGeneratorDemo {

	public static void main(String[] args) {
        System.out.println(Utils.getProjectPath());
        EntityGenerator.Config config = new EntityGenerator.Config("user",
                Utils.getProjectPath()+"\\src\\main\\java",
                "org.demo.entity",
                "org.demo.persistence");
		config.setForceCoverDao(false);
		config.setForceCoverEntity(true);
		config.setCreateDao(true);
		EntityGenerator.generateEntity(config);
	}
    
}
