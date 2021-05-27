package org.jujubeframework.jdbc.persistence;

import org.jujubeframework.jdbc.client.local.LocalJdbcTemplate;
import org.jujubeframework.jdbc.generator.EntityGenerator;
import org.jujubeframework.util.Jsons;
import org.jujubeframework.util.Resources;

import java.util.List;

public class EntityGeneratorDemo {

    public static void main(String[] args) {
        generateOneEntity();
    }

    public static void getTables() {
        List<String> artfoxlivedb = LocalJdbcTemplate.getTables("artfoxlivedb");
        System.out.println(Jsons.toJson(artfoxlivedb));
    }

    public static void generateEntity() {
        List<String> artfoxlivedb = LocalJdbcTemplate.getTables("artfoxlivedb");
        for (String tableName : artfoxlivedb) {
            EntityGenerator.Config config = new EntityGenerator.Config(tableName, Resources.getProjectPath() + "\\src\\main\\java", "com.artfoxlive.entity",
                    "com.artfoxlive.newpersistence");
            config.setForceCoverDao(false);
            config.setForceCoverEntity(true);
            config.setCreateDao(true);
            config.setCreateDaoSqlFile(true);
            EntityGenerator.generateEntity(config);
            System.out.println();
        }
    }

    public static void generateOneEntity() {
        // String projectPath = Resources.getProjectPath();
        // String projectPath = "E:\\works\\artfox\\artfox-service\\persistence";
        String projectPath = "D:\\workspace\\artfox\\artfox-service\\persistence";
        // String projectPath = "D:\\workspace\\artfox-service\\persistence";
        EntityGenerator.Config config = new EntityGenerator.Config("bigdata_website_operation_metrics", projectPath + "\\src\\main\\java", "com.artfoxlive.entity",
                "com.artfoxlive.newpersistence");
        config.setForceCoverDao(false);
        config.setForceCoverEntity(true);
        config.setCreateDao(true);
        config.setCreateDaoSqlFile(true);
        config.setRemoveColumnPrefix(false);
        config.setColumnPrefix("");
        config.setRemoveColumnSuffix(false);
        config.setColumnSuffix("");
        config.setAddColumnAnnotation(false);
        EntityGenerator.generateEntity(config);
    }
}
