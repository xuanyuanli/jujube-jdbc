package org.dazao.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 集合工具类。区别于jdk的Collections和guava的Collections2 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Collections3 {

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List<String>.
     * <p>
     * 不同于Collections3，这里返回String集合
     * 
     * @param collection
     *            来源集合.
     * @param propertyName
     *            要提取的属性名.
     */
    public static List<String> extractToListString(final Collection<?> collection, final String propertyName) {
        List<String> list = new ArrayList<>(collection.size());

        try {
            for (Object obj : collection) {
                list.add(Beans.getPropertyAsString(obj, propertyName));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /**
     * 根据条件，从集合中取出一个
     * 
     * @since 2014年3月25日 下午5:32:25
     * @author 李衡 Email：li15038043160@163.com
     * @param coll
     *            集合
     * @param fieldName
     *            字段名
     * @param value
     *            字段值
     */
    public static <T> T getOne(Collection<T> coll, String fieldName, Object value) {
        Validate.notNull(coll);
        T result = null;
        for (T t : coll) {
            if (value.equals(Beans.getProperty(t, fieldName))) {
                result = t;
                break;
            }
        }
        return result;
    }

    /**
     * 根据条件，从集合中取出符合条件的部分
     * 
     * @author 李衡 Email：li15038043160@163.com
     * @param coll
     *            集合
     * @param fieldName
     *            字段名
     * @param value
     *            字段值
     */
    public static <T> List<T> getPart(Collection<T> coll, String fieldName, Object value) {
        Validate.notNull(coll);
        return coll.stream().filter(t -> value.equals(Beans.getProperty(t, fieldName))).collect(Collectors.toList());
    }

    /**
     * 字符串数组去重
     */
    public static String[] toDiffArray(String[] s) {
        Set<String> set = new LinkedHashSet<String>();
        for (String sa : s) {
            set.add(sa);
        }
        return set.toArray(new String[] {});
    }

    /**
     * 提取集合中的对象的两个属性(通过Getter函数), 组合成Map.
     * 
     * @param collection
     *            来源集合.
     * @param keyPropertyName
     *            要提取为Map中的Key值的属性名.
     * @param valuePropertyName
     *            要提取为Map中的Value值的属性名.
     */
    public static <T> Map<Object, Object> extractToMap(final Collection<T> collection, final String keyPropertyName, final String valuePropertyName) {
        Map<Object, Object> map = new HashMap<>(collection.size());

        try {
            for (Object obj : collection) {
                map.put(PropertyUtils.getProperty(obj, keyPropertyName), PropertyUtils.getProperty(obj, valuePropertyName));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List.
     * 
     * @param collection
     *            来源集合.
     * @param propertyName
     *            要提取的属性名.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> List<T> extractToList(final Collection collection, final String propertyName) {
        if (collection == null) {
            return null;
        }
        List list = new ArrayList(collection.size());

        try {
            for (Object obj : collection) {
                list.add(PropertyUtils.getProperty(obj, propertyName));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成由分割符分隔的字符串.
     * 
     * @param collection
     *            来源集合.
     * @param propertyName
     *            要提取的属性名.
     * @param separator
     *            分隔符.
     */
    @SuppressWarnings({ "rawtypes" })
    public static String extractToString(final Collection collection, final String propertyName, final String separator) {
        List list = extractToList(collection, propertyName);
        return StringUtils.join(list, separator);
    }

    /**
     * 转换Collection所有元素(通过toString())为String, 中间以 separator分隔。
     */
    @SuppressWarnings({ "rawtypes" })
    public static String convertToString(final Collection collection, final String separator) {
        return StringUtils.join(collection, separator);
    }

    /**
     * 转换Collection所有元素(通过toString())为String,
     * 每个元素的前面加入prefix，后面加入postfix，如<div>mymessage</div>。
     */
    @SuppressWarnings({ "rawtypes" })
    public static String convertToString(final Collection collection, final String prefix, final String postfix) {
        StringBuilder builder = new StringBuilder();
        for (Object o : collection) {
            builder.append(prefix).append(o).append(postfix);
        }
        return builder.toString();
    }

    /**
     * 判断是否为空.
     */
    @SuppressWarnings({ "rawtypes" })
    public static boolean isEmpty(Collection collection) {
        return (collection == null) || collection.isEmpty();
    }

    /**
     * 判断是否为空.
     */
    @SuppressWarnings({ "rawtypes" })
    public static boolean isEmpty(Map map) {
        return (map == null) || map.isEmpty();
    }

    /**
     * 判断是否为空.
     */
    @SuppressWarnings({ "rawtypes" })
    public static boolean isNotEmpty(Collection collection) {
        return (collection != null) && !(collection.isEmpty());
    }

    /**
     * 取得Collection的第一个元素，如果collection为空返回null.
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.iterator().next();
    }

    /**
     * 获取Collection的最后一个元素 ，如果collection为空返回null.
     */
    public static <T> T getLast(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }

        // 当类型为List时，直接取得最后一个元素 。
        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            return list.get(list.size() - 1);
        }

        // 其他类型通过iterator滚动到最后一个元素.
        Iterator<T> iterator = collection.iterator();
        while (true) {
            T current = iterator.next();
            if (!iterator.hasNext()) {
                return current;
            }
        }
    }

    /**
     * 返回a+b的新List.
     */
    public static <T> List<T> union(final Collection<T> a, final Collection<T> b) {
        List<T> result = new ArrayList<T>(a);
        result.addAll(b);
        return result;
    }

    /**
     * 返回a-b(集合a中有，而b中没有)的新List.
     */
    public static <T> List<T> subtract(final Collection<T> a, final Collection<T> b) {
        List<T> list = new ArrayList<>(a);
        for (Object element : b) {
            list.remove(element);
        }
        return list;
    }

    /**
     * 返回a与b的交集的新List.
     */
    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        List<T> list = new ArrayList<T>();

        for (T element : a) {
            if (b.contains(element)) {
                list.add(element);
            }
        }
        return list;
    }

    public static <T> List<T> enumerationToList(Enumeration<T> enumeration) {
        List<T> list = new ArrayList<T>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /** 对Map键值进行反转 */
    public static Map<?, ?> reversalMap(Map<?, ?> map) {
        Map<Object, Object> result = new HashMap<>();
        for (Object key : map.keySet()) {
            result.put(map.get(key), key);
        }
        return result;
    }

    /** 根据key排序map */
    public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map, Comparator<K> comparator) {
        Map<K, V> result = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entryList = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return comparator.compare(o1.getKey(), o2.getKey());
            }
        });
        for (Entry<K, V> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /** 根据value排序map */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Comparator<V> comparator) {
        Map<K, V> result = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entryList = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return comparator.compare(o1.getValue(), o2.getValue());
            }
        });
        for (Entry<K, V> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /** 是否存在集合中的字段值为 detectVal(只用于判断基本类型) */
    public static boolean containsFieldValue(Collection<?> source, String fieldName, Object detectVal) {
        if (source == null) {
            return false;
        }
        return source.stream().anyMatch(t -> detectVal.equals(Beans.getProperty(t, fieldName)));
    }

    /** 根据某个字段去重 */
    public static <T> List<T> distinctByProperty(Collection<T> serverServices, String propertyName) {
        List<T> list = new ArrayList<>();
        Set<Object> diffArray = new HashSet<>(extractToList(serverServices, propertyName));
        for (Object val : diffArray) {
            list.add(getOne(serverServices, propertyName, val));
        }
        return list;
    }

    /** 分组某个集合中的字段值 */
    public static <T> Set<String> groupBy(Collection<T> data, String fieldName) {
        Set<String> set = new LinkedHashSet<>();
        for (T t : data) {
            set.add(Beans.getPropertyAsString(t, fieldName));
        }
        return set;
    }

}
