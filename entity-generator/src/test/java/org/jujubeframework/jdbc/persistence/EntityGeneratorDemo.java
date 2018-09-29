package org.jujubeframework.jdbc.persistence;

import org.jujubeframework.jdbc.generator.EntityGenerator;
import org.jujubeframework.util.Resources;

public class EntityGeneratorDemo {

	public static void main(String[] args) {
        EntityGenerator.Config config = new EntityGenerator.Config("user",
                Resources.getProjectPath()+"\\src\\main\\java",
                "org.demo.entity",
                "org.demo.persistence");
		config.setForceCoverDao(false);
		config.setForceCoverEntity(true);
		config.setCreateDao(true);
		EntityGenerator.generateEntity(config);
	}
    
}
