package com.yfs.util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 为减少技术选型，使用Spring内置的Jackson作为json转换工具
 *
 * @author 李衡 Email：li15038043160@163.com
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Jsons {

    private static Logger logger = LoggerFactory.getLogger(Jsons.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        // 反序列化的时候，如果类中不存在属性，设置为忽略；如果不用这个设置，可以使用 @JsonIgnoreProperties注解类(或
        // @JsonIgnore注解字段)到对应字段
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
        OBJECT_MAPPER.configure(Feature.ALLOW_SINGLE_QUOTES, true);

        // 过滤对象的null属性.
        // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 过滤map中的null值
        // objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,
        // false);
    }

    /** 将对象转换为json字符串 */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("write to json string error:" + object, e);
        }
        return null;
    }

    /** 将对象转换为格式化的json字符串 */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("write to json string error:" + object, e);
        }
        return null;
    }

    /** 将json字符串转换为Map */
    public static Map<String, Object> parseJsonToMap(String text) {
        return parseJson(text, new TypeReference<Map<String, Object>>() {
        });
    }

    /** 将json字符串转换为List Long */
    public static List<Long> parseJsonToListLong(String text) {
        return parseJson(text, new TypeReference<List<Long>>() {
        });
    }

    /** 将json字符串转换为List String */
    public static List<String> parseJsonToListString(String text) {
        return parseJson(text, new TypeReference<List<String>>() {
        });
    }

    /**
     * 将json字符串转换为复杂类型（如泛型）的Java对象（万能，常用）
     * 
     * <pre>
     * 用法：
     *  1、将json字符串转换为User对象：parseJson(text,new TypeReference<User>(){})
     *  2、将json字符串转换为List<String>的泛型：parseJson(text,new TypeReference<List<String>>(){})
     *  3、将json字符串转换为List<Map<String,Object>>的泛型：parseJson(text,new TypeReference<List<Map<String,Object>>>(){})
     * </pre>
     * 
     * @param text
     *            json字符串
     * @param typeReference
     *            类型引用
     */
    public static <T> T parseJson(String text, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (IOException e) {
            logger.error("parseJson", e);
        }
        return null;
    }

    /** 将json字符串转换为对应类型的Java对象（不常用） */
    @SuppressWarnings("unchecked")
    public static <T> T parseJson(String text, Type type) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        if (String.class.getTypeName().equals(type.getTypeName())) {
            return (T) text;
        }
        try {
            return OBJECT_MAPPER.readValue(text, TypeFactory.defaultInstance().constructType(type));
        } catch (IOException e) {
            logger.error("parseJson", e);
        }
        return null;
    }

    /** 将json字符串转换为对应类型的Java对象（常用） */
    public static <T> T parseJson(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (IOException e) {
            logger.error("parseJson", e);
        }
        return null;
    }

    public static JsonNode readTree(String text) {
        try {
            return OBJECT_MAPPER.readTree(text);
        } catch (IOException e) {
            logger.error("readTree", e);
        }
        return null;
    }

    /** 把json转换为pretty输出格式 */
    public static String prettyPrint(String json) {
        return toPrettyJson(parseJsonToMap(json));
    }

}
