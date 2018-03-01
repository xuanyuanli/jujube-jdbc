package com.yfs.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import com.yfs.constant.Constants;
import com.yfs.support.freemarker.ClassloaderTemplateLoader;

import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;

/**
 * 项目的FreeMarker总体配置类。直接调用其中方法生成模板
 * 
 * @author 李衡 Email：li15038043160@163.com
 */
public class Ftls {
    /** 模板总目录 */
    private static final String FTL_DIR = "templates";

    private Ftls() {
    }

    private static Configuration file_template_configuration;// 文件模板
    private static Configuration string_template_configuration;// 字符串模板
    private static StringTemplateLoader stringTemplateLoader; // 字符串模板载入器
    static {
        Properties props = new Properties();
        props.put("tag_syntax", "auto_detect");
        props.put("template_update_delay", "5");
        props.put("defaultEncoding", Constants.PROJECT_ENCODING.name());
        props.put("url_escaping_charset", Constants.PROJECT_ENCODING.name());
        props.put("boolean_format", "true,false");
        props.put("datetime_format", "yyyy-MM-dd HH:mm:ss");
        props.put("date_format", "yyyy-MM-dd");
        props.put("time_format", "HH:mm:ss");
        props.put("number_format", "0.######");
        props.put("whitespace_stripping", "true");

        file_template_configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

        file_template_configuration.setTemplateLoader(new ClassloaderTemplateLoader(FTL_DIR));
        try {
            file_template_configuration.setSettings(props);
        } catch (TemplateException e) {
        }

        string_template_configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        string_template_configuration.setDefaultEncoding(Constants.PROJECT_ENCODING.name());
        stringTemplateLoader = new StringTemplateLoader();
        string_template_configuration.setTemplateLoader(stringTemplateLoader);
        try {
            string_template_configuration.setSettings(props);
        } catch (TemplateException e) {
        }
    }

    /**
     * 生成模板到文件
     * 
     * @param templateName
     *            模板名称
     * @param outputPath
     *            输出路径(绝对路径)
     * @param root
     *            FreeMarker数据模型
     */
    public static void processFileTemplateToFile(String templateName, String outputPath, Map<String, Object> root) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(Utils.createFile(outputPath)), Constants.PROJECT_ENCODING);) {
            processFileTemplateTo(templateName, root, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成模板，输出到控制台
     * 
     * @param templateName
     *            模板名称
     * @param root
     *            FreeMarker数据模型
     */
    public static void processFileTemplateToConsole(String templateName, Map<String, Object> root) {
        processFileTemplateTo(templateName, root, new OutputStreamWriter(System.out));
    }

    /**
     * 生成模板，输出到控制台
     * 
     * @param templateName
     *            模板名称
     * @param root
     *            FreeMarker数据模型
     */
    public static String processFileTemplateToString(String templateName, Map<String, Object> root) {
        return processTemplateToString(getFileTemplate(templateName), root);
    }

    private static void processFileTemplateTo(String templateName, Map<String, Object> root, Writer out) {
        processTemplateTo(getFileTemplate(templateName), root, out);
    }

    /**
     * 处理模板源文件，生成内容
     * 
     * @param ftlSource
     *            模板源码
     * @param map
     *            root
     */
    public static String processStringTemplateToString(String ftlSource, Map<String, Object> map) {
        String defaultFtlName = "default_" + ftlSource.hashCode();
        stringTemplateLoader.putTemplate(defaultFtlName, ftlSource);
        try {
            Template template = string_template_configuration.getTemplate(defaultFtlName);
            return processTemplateToString(template, map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Template getFileTemplate(String templateName) {
        try {
            return file_template_configuration.getTemplate(templateName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String processTemplateToString(Template template, Map<String, Object> map) {
        StringWriter result = new StringWriter();
        processTemplateTo(template, map, result);
        return result.toString();
    }

    /**
     * 生成模板，输出到...
     * 
     * @author 李衡 Email：li15038043160@163.com
     */
    private static void processTemplateTo(Template template, Map<String, Object> root, Writer out) {
        try {
            template.process(root, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    static BeansWrapper wrapper = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();
    static TemplateHashModel staticModels = wrapper.getStaticModels();

    public static TemplateHashModel useStaticPackage(Class<?> clazz) {
        try {
            TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(clazz.getName());
            return fileStatics;
        } catch (Exception e) {
            return null;
        }
    }

}
