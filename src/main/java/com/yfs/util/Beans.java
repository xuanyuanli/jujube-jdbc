package com.yfs.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

import javassist.Modifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.sf.cglib.beans.BeanMap;

/**
 * 关于类操作的，都在这里<br>
 * 用到工具栏BeanUtils，把抛出的异常屏蔽了<br>
 * 其他类操作工具类，参考：FieldUtils、MethodUtils等。如果不能满足需求，可以自己实现
 * 
 * @author 李衡 Email：li15038043160@163.com
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Beans {

    private static Logger logger = LoggerFactory.getLogger(Beans.class);

    /**
     * 把对象转换为map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> beanToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return BeanMap.create(obj);
    }

    public static <T> T getInstance(Class<T> cl) {
        try {
            return cl.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> forName(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see BeanUtils#setProperty(Object bean, String name, Object value)
     */
    public static void setProperty(Object bean, String name, Object value) {
        try {
            BeanUtils.setProperty(bean, name, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 自己实现的set方法(解决链式调用后setProperty不管用的情况)
     */
    public static void setProperty2(Object bean, String name, Object value) {
        PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(), name);
        Class<?> type = descriptor.getPropertyType();
        Method writeMethod = descriptor.getWriteMethod();

        Object newValue = value;
        if (!type.equals(value.getClass())) {
            ConvertUtilsBean convertUtilsBean = BeanUtilsBean.getInstance().getConvertUtils();
            if (value instanceof String) {
                newValue = convertUtilsBean.convert((String) value, type);
            } else {
                newValue = convert(convertUtilsBean, value, type);
            }
        }
        Beans.invoke(writeMethod, bean, newValue);
    }

    protected static Object convert(ConvertUtilsBean convertUtilsBean, final Object value, final Class<?> type) {
        final Converter converter = convertUtilsBean.lookup(type);
        if (converter != null) {
            return converter.convert(type, value);
        } else {
            return value;
        }
    }

    /**
     * 通过getter方法来获取值
     * 
     * @see BeanUtils#getProperty(Object bean, String name)
     */
    public static Object getProperty(Object bean, String name) {
        try {
            return PropertyUtils.getProperty(bean, name);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 通过getter方法来获取值
     * 
     * @see BeanUtils#getProperty(Object bean, String name)
     */
    public static String getPropertyAsString(Object bean, String name) {
        try {
            return BeanUtils.getProperty(bean, name);
        } catch (Exception e) {
        }
        return null;
    }

    /** 获得所有的public方法 */
    public static Method getMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = cl.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
        }
        return method;
    }

    /** 获得类的所有声明方法，包括父类中的 */
    public static Method getDeclaredMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = getSelfDeclaredMethod(cl, methodName, parameterTypes);
            for (; cl != Object.class && method == null; cl = cl.getSuperclass()) {
                method = getSelfDeclaredMethod(cl, methodName, parameterTypes);
            }
            return method;
        } catch (Exception e) {
        }
        return null;
    }

    /** 获得类的所有声明方法，不包括父类中的 */
    public static Method getSelfDeclaredMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = cl.getDeclaredMethod(methodName, parameterTypes);
            return method;
        } catch (Exception e) {
        }
        return null;
    }

    /** 反射调用方法 */
    public static Object invoke(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 获得类的所有声明字段，包括父类中的 */
    public static Field getDeclaredField(Class<?> cl, String fieldName) {
        try {
            Field field = getSelfDeclaredField(cl, fieldName);
            for (; cl != Object.class && field == null; cl = cl.getSuperclass()) {
                field = getSelfDeclaredField(cl, fieldName);
            }
            return field;
        } catch (Exception e) {
        }
        return null;
    }

    /** 获得类的所有声明字段，不包括父类中的 */
    public static Field getSelfDeclaredField(Class<?> cl, String fieldName) {
        try {
            return cl.getDeclaredField(fieldName);
        } catch (Exception e) {
        }
        return null;
    }

    private static BeanInfo getBeanInfo(Class<?> targetClass) {
        try {
            return Introspector.getBeanInfo(targetClass);
        } catch (final IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    /** key为classname+fieldName */
    private static final ConcurrentMap<String, PropertyDescriptor> PROPERTY_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    /** 获得类的某个字段属性描述 */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> targetClass, String fieldName) {
        String key = targetClass.getName() + ":" + fieldName;
        if (PROPERTY_DESCRIPTOR_CACHE.containsKey(key)) {
            return PROPERTY_DESCRIPTOR_CACHE.get(key);
        } else {
            PropertyDescriptor descriptor = null;
            BeanInfo beanInfo = getBeanInfo(targetClass);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                String curFieldName = fieldName;
                if (fieldName.length() >= 2 && Character.isUpperCase(fieldName.charAt(1))) { // 解决第二个字母为大写的情况（第二个字母为大写的话，propertyDescriptor就会出现前两个字母都为大写的情况）
                    curFieldName = Texts.capitalize(fieldName);
                }
                if (propertyDescriptor.getName().equals(curFieldName)) {
                    descriptor = propertyDescriptor;
                    break;
                }
            }
            if (descriptor != null) {
                if (descriptor.getWriteMethod() == null) { // 如果用lombok的@Accessors(chain=true)注解的话(链式操作)，writeMethod会为空
                    String methodName = "set" + StringUtils.capitalize(fieldName);
                    Method writeMethod = Beans.getDeclaredMethod(targetClass, methodName, descriptor.getPropertyType());
                    try {
                        descriptor.setWriteMethod(writeMethod);
                    } catch (IntrospectionException e) {
                        throw new RuntimeException(e);
                    }
                }
                PROPERTY_DESCRIPTOR_CACHE.put(key, descriptor);
            }
            return descriptor;
        }
    }

    /**
     * 获得形参名和形参值的简单对照表（name-value）
     * 
     * @param method
     *            方法
     * @param args
     *            实参集合(可为空，MethodParam的value也为空)
     */
    public static Map<String, Object> getFormalParamSimpleMapping(Method method, Object args[]) {
        Map<String, Object> result = new HashMap<>();
        String[] names = getMethodParamNames(method);
        Class<?>[] types = method.getParameterTypes();
        boolean existValue = args != null && args.length > 0;
        for (int i = 0; i < types.length; i++) {
            String fname = names[i];
            Object value = null;
            if (existValue) {
                value = args[i];
            }
            result.put(fname, value);
        }
        return result;
    }

    /**
     * 
     * 比较参数类型是否一致
     *
     * @param types
     *            asm的类型({@link Type})
     * @param clazzes
     *            java 类型({@link Class})
     * @return
     */
    private static boolean sameType(Type[] types, Class<?>[] clazzes) {
        // 个数不同
        if (types.length != clazzes.length) {
            return false;
        }

        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(clazzes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }

    /** getMethodParamNames的缓存，key为方法名，value为形参列表 */
    private static final ConcurrentMap<String, String[]> METHOD_PARAMNAMES_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取方法的形参名集合
     */
    public static String[] getMethodParamNames(final Method method) {
        String key = method.toString();
        if (METHOD_PARAMNAMES_CACHE.containsKey(key)) {
            return METHOD_PARAMNAMES_CACHE.get(key);
        } else {
            final String[] paramNames = new String[method.getParameterTypes().length];
            final String className = method.getDeclaringClass().getName();
            final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            try {
                ClassReader classReader = new ClassReader(Utils.getDefaultClassLoader().getResourceAsStream(className.replace('.', '/') + ".class"));
                classReader.accept(new ClassVisitor(Opcodes.ASM4, classWriter) {
                    @Override
                    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
                        final Type[] args = Type.getArgumentTypes(desc);
                        // 方法名相同并且参数个数相同
                        if (!name.equals(method.getName()) || !sameType(args, method.getParameterTypes())) {
                            return super.visitMethod(access, name, desc, signature, exceptions);
                        }
                        MethodVisitor v = cv.visitMethod(access, name, desc, signature, exceptions);
                        return new MethodVisitor(Opcodes.ASM4, v) {
                            @Override
                            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                                int i = index - 1;
                                // 如果是静态方法，则第一就是参数
                                // 如果不是静态方法，则第一个是"this"，然后才是方法的参数
                                if (Modifier.isStatic(method.getModifiers())) {
                                    i = index;
                                }
                                if (i >= 0 && i < paramNames.length) {
                                    paramNames[i] = name;
                                }
                                super.visitLocalVariable(name, desc, signature, start, end, index);
                            }

                        };
                    }
                }, 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            METHOD_PARAMNAMES_CACHE.putIfAbsent(key, paramNames);
            return paramNames;
        }
    }

    /** 获得所有可访问的字段名（包括父类）集合 */
    public static List<String> getAllDeclaredFieldNames(Class<?> clazz) {
        BeanInfo beanInfo = getBeanInfo(clazz);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        List<String> fields = new ArrayList<>(propertyDescriptors.length);
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String fieldName = descriptor.getName();
            if (!fieldName.equals("class")) { // 去除class字段
                fields.add(fieldName);
            }
        }
        return fields;
    }

    /**
     * 对比两个对象，获取差异字段集合
     * 
     * @param oldObject
     *            旧对象
     * @param newObject
     *            新对象
     */
    public static List<FieldDidderence> contrastObject(Object oldObject, Object newObject) {
        List<FieldDidderence> result = new ArrayList<>();
        Field[] noFields = newObject.getClass().getDeclaredFields();
        for (Field noField : noFields) {
            String fieldName = noField.getName();
            Object noValue = Beans.getProperty(newObject, fieldName);
            if (noValue != null) {// 如果字段不为空，则表示该字段修改
                Object oldValue = Beans.getProperty(oldObject, fieldName);
                if (!(noValue.equals(oldValue))) {
                    FieldDidderence didderence = new FieldDidderence();
                    didderence.setFiledName(fieldName);
                    didderence.setNewValue(noValue.toString());
                    if (oldValue != null) {
                        didderence.setOldValue(oldValue.toString());
                    } else {
                        didderence.setOldValue("");
                    }
                    result.add(didderence);
                }
            }
        }
        return result;
    }

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /** 获得包下的所有class */
    public static List<Class<?>> getPackageClasses(String packageName) {
        List<Class<?>> list = new ArrayList<>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
        try {
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    String filePath = URLDecoder.decode(resource.getURL().getFile(), "UTF-8");
                    String className = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length() - 6);
                    if (!className.contains("$")) {
                        try {
                            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(getRealPackageName(filePath, packageName) + "." + className);
                            list.add(clazz);
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("resourcePatternResolver.getResources", e);
        }
        return list;
    }

    private static String getRealPackageName(String filePath, String packageName) {
        String pName = packageName.replace(".", "/");
        String tPath = filePath.substring(filePath.indexOf(pName));
        pName = tPath.substring(0, tPath.lastIndexOf("/"));
        return pName.replace("/", ".");
    }

    /**
     * 通过反射, 获得Class定义中声明的泛型参数的类型, 注意泛型必须定义在父类处 如无法找到, 返回Object.class.<br>
     * 
     * @param clazz
     *            The class to introspect
     * @return the first generic declaration, or Object.class if cannot be
     *         determined
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassGenericType(final Class<?> clazz) {
        return (Class<T>) getClassGenericType(clazz, 0);
    }

    /**
     * 通过反射, 获得Class定义中声明的父类的泛型参数的类型. 如无法找到, 返回Object.class.
     * 
     * @param clazz
     *            clazz The class to introspect
     * @param index
     *            the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be
     *         determined
     */
    public static Class<?> getClassGenericType(final Class<?> clazz, final int index) {
        java.lang.reflect.Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        java.lang.reflect.Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if ((index >= params.length) || (index < 0)) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class<?>) params[index];
    }

    /** 基本类型封装类列表 */
    private final static List<Class<?>> BASIC_TYPE = Arrays.asList(Double.class, String.class, Float.class, Byte.class, Integer.class, Character.class, Long.class, Short.class);

    /** 是否是基本数据类型 */
    public static boolean isBasicType(Class<?> cl) {
        return cl.isPrimitive() || BASIC_TYPE.contains(cl);
    }

    /** 字段差异 */
    public static class FieldDidderence {
        private String filedName;// 字段名称
        private String oldValue;// 字段修改前的值
        private String newValue;// 字段修改后的值

        public String getFiledName() {
            return filedName;
        }

        public void setFiledName(String filedName) {
            this.filedName = filedName;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

    }

}
