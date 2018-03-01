package com.yfs.support.freemarker;

import java.net.URL;

import org.springframework.util.ClassUtils;

import freemarker.cache.URLTemplateLoader;

/**
 * 为解决不能读取jar中目录的问题，拓展Freemarker的TemplateLoader
 */
public class ClassloaderTemplateLoader extends URLTemplateLoader {
    private String path;

    public ClassloaderTemplateLoader(String path) {
        super();
        this.path = canonicalizePrefix(path);
    }

    @Override
    protected URL getURL(String name) {
        name = path + name;
        return ClassUtils.getDefaultClassLoader().getResource(name);
    }

}
