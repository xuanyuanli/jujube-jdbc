package com.yfs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.yfs.constant.Constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 公共工具方法集合 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * 获取所有classpath下对应名称的Properties文件属性
     * 
     * @param fileName
     *            相对于classpath的文件位置
     */
    public static Properties getProperties(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName不能为空");
        }

        Properties properties = new Properties();
        try {
            Resource[] resources = getClassPathAllResources(fileName);
            for (int i = resources.length - 1; i >= 0; i--) {
                Resource resource = resources[i];
                properties.load(resource.getInputStream());
                logger.debug("load properties:{}", resource.getURL());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    /**
     * 获取当前classpath下对应名称的Properties文件属性
     * 
     * @param fileName
     *            相对于classpath的文件位置
     */
    public static Properties getCurrentClasspathProperties(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName不能为空");
        }

        Properties properties = new Properties();
        Resource resource = getClassPathResources(fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            properties.load(inputStream);
            logger.debug("load properties:{}", resource.getURL());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    /** 获得classpath*下的指定资源 */
    public static Resource[] getClassPathAllResources(String resourceName) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = null;
        try {
            resources = resolver.getResources("classpath*:" + resourceName);
        } catch (IOException e) {
        }
        return resources;
    }

    /** 获得classpath下的指定资源 */
    public static Resource getClassPathResources(String resourceName) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = null;
        try {
            resources = resolver.getResources("classpath:" + resourceName);
        } catch (IOException e) {
        }
        return resources != null && resources.length > 0 ? resources[0] : null;
    }

    /**
     * 抛出运行时异常
     */
    public static void throwException(Throwable e) {
        throw new RuntimeException(e);
    }

    /** 获得异常堆栈信息 */
    public static String exceptionToString(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /** 获得异常堆栈信息 */
    public static String exceptionToString(Exception exception, int len) {
        String data = exceptionToString(exception);
        if (data != null && data.length() > len) {
            data = data.substring(0, len);
        }
        return data;
    }

    /**
     * 数字格式化
     */
    public static String numberFormat(Number number, String pattern) {
        DecimalFormat myformat = new DecimalFormat();
        myformat.applyPattern(pattern);
        return myformat.format(number);
    }

    /** 创建目录 */
    public static File createDir(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        File myFile = new File(filePath);
        if (!myFile.exists()) {
            myFile.mkdirs();
        }
        return myFile;
    }

    /**
     * 创建文件。如果上级路径不存在，则创建路径；如果文件不存在，则创建文件
     * 
     * @param filePath
     *            文件绝对路径
     * @return
     */
    public static File createFile(String filePath) {
        Validate.isTrue(StringUtils.isNotBlank(filePath), "文件路径不能为空");
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * 获得当前项目（jar）的ClassLoader
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = Utils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap
                // ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the
                    // caller can live with null...
                }
            }
        }
        return cl;
    }

    /** 获得某个class所在的jar所在的目录（例如打包A项目为jar，获取的就是A.jar所在的目录） */
    public static String getJarHome(Class<?> cl) {
        String path = cl.getProtectionDomain().getCodeSource().getLocation().getFile();
        File jarFile = new File(path);
        return jarFile.getParentFile().getAbsolutePath();
    }

    /**
     * 获得当前的classpath目录
     */
    public static File getCurrentClasspath() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:./");
        try {
            return resource.getFile();
        } catch (IOException e) {
            return null;
        }
    }

    /** 获得当前项目路径（只限于Eclipse中管用） */
    public static String getProjectPath() {
        File dir = Utils.getCurrentClasspath();
        if (dir != null) {
            // 找到target目录
            while (true) {
                if (dir.getName().equals("target")) {
                    break;
                } else {
                    dir = dir.getParentFile();
                }
            }
            String projectDir = dir.getParentFile().getAbsolutePath();
            return projectDir;
        }
        return null;
    }

    /**
     * 将一个转义后的符号，反转义回来
     */
    public static String[] unescape(String[] params, String escape) {
        String str = StringEscapeUtils.unescapeHtml4(escape);
        String[] result = new String[params.length];
        int i = 0;
        for (String string : params) {
            if (string.contains(escape)) {
                string = string.replace(escape, str);
            }
            result[i] = string;
            i++;
        }
        return result;
    }

    /** 对数组进行trim，摒弃数组中的空值 */
    public static String[] trim(String[] params) {
        List<String> result = new ArrayList<>();
        for (String string : params) {
            if (!StringUtils.isBlank(string.trim()) || !string.equals("")) {
                result.add(string);
            }
        }
        return result.toArray(new String[0]);
    }

    public static Map<String, Object> newHashMap(Object... keyValue) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (keyValue == null || keyValue.length == 0) {
            return map;
        }
        if (keyValue.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < keyValue.length; i += 2) {
            map.put(String.valueOf(keyValue[i]), keyValue[i + 1]);
        }
        return map;
    }

    /** 是否是Jar中的文件 */
    public static boolean isJarFile(URL url) {
        return url.getProtocol().equals("jar");
    }

    /** One kilobyte bytes. */
    public static final long ONE_KB = 1024;

    /** One megabyte bytes. */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /** One gigabyte bytes. */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * Returns <code>size</code> in human-readable units (GB, MB, KB or bytes).
     */
    public static String humanReadableUnits(long bytes) {
        return humanReadableUnits(bytes, new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT)));
    }

    /**
     * Returns <code>size</code> in human-readable units (GB, MB, KB or bytes).
     */
    public static String humanReadableUnits(long bytes, DecimalFormat df) {
        if (bytes / ONE_GB > 0) {
            return df.format((float) bytes / ONE_GB) + " GB";
        } else if (bytes / ONE_MB > 0) {
            return df.format((float) bytes / ONE_MB) + " MB";
        } else if (bytes / ONE_KB > 0) {
            return df.format((float) bytes / ONE_KB) + " KB";
        } else {
            return bytes + " bytes";
        }
    }

    /** 打印运行时堆栈日志到debug logger */
    public static void printRuntimeStack(String taskName) {
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            Constants.LOGGER_DEBUG.info("--------------start | " + taskName);
            for (int i = 0; i < stackElements.length; i++) {
                Constants.LOGGER_DEBUG.info("{}", stackElements[i]);
            }
            Constants.LOGGER_DEBUG.info("\n\n");
        }

    }

    /** 获得当前jvm的标识 */
    public static String getJVMFlag() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    /** 获取本地MAC地址的方法 */
    public static String getLocalMacAddress() {
        try {
            InetAddress ia = InetAddress.getLocalHost();// 获取本地IP对象
            // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

            // 下面代码是把mac地址拼装成String
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            // 把字符串所有小写字母改为大写成为正规的mac地址并返回
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            return "TT-TT-TT-TT";
        }
    }

    /** 获得主机名 */
    public static String getHostName() {
        InetAddress netAddress;
        try {
            netAddress = InetAddress.getLocalHost();
            return netAddress.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static void bindPort(String host, int port) throws Exception {
        try (Socket s = new Socket()) {
            s.bind(new InetSocketAddress(host, port));
        }
    }

    /** 端口是否可用 */
    public static boolean isPortAvailable(int port) {
        try {
            bindPort("0.0.0.0", port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 判断ip是否是本地IP */
    public static boolean isLocalIp(String qip) {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (!netInterface.isUp() || netInterface.isLoopback() || netInterface.isVirtual())
                    continue;
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address && ip.getHostAddress().equals(qip)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /** 把number转换为string，非科学计数法 */
    public static String numberToString(Number value) {
        DecimalFormat decimalFormat = new DecimalFormat("###########.##########");// 格式化设置
        decimalFormat.setGroupingUsed(false);
        return decimalFormat.format(value);
    }

    /** 此Class是否从jar中启动 */
    public static boolean isJarStartByClass(Class<?> cl) {
        return cl.getResource(cl.getSimpleName() + ".class").getProtocol().equals("jar");
    }
}
