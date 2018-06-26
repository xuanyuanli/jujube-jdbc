package org.dazao.persistence;

import org.dazao.generator.EntityGenerator;
import org.dazao.generator.EntityGenerator.Config;

public class EntityGeneratorDemo {

	public static void main(String[] args) {
		Config config = new Config("user", "D:\\workspace\\easy-jdbc\\entity-generator\\src\\main\\java", "org.demo.entity", "org.demo.persistence");
		config.setForceCoverDao(false);
		config.setForceCoverEntity(true);
		config.setCreateDao(true);
		EntityGenerator.generateEntity(config);
	}
    
}
