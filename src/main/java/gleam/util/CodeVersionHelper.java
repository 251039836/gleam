package gleam.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 代码版本辅助类
 * 
 * @author LH
 * @date 2021/5/18 15:00
 */
@SuppressWarnings("unused")
public class CodeVersionHelper {
    /**
     * jar包Manifest文件路径
     */
    private final static String MANIFEST_FILE = "META-INF/MANIFEST.MF";
    /**
     * 在manifest中版本号的字段名
     */
    private final static String VERSION_FIELD = "Sources-Code-Version";
    /**
     * 测试环境的临时版本文件
     */
    private final static String VERSION_FILE = "ver.txt";
    /**
     * 正式环境下的版本号文件
     */
    private final static String RELEASE_VERSION_FILE = "version.txt";
    /** 正式发行包的主版本号 */
    private final static int RELEASE_MAJOR_VERSION_INDEX = 0;
    /** 正式发行包的次版本号 对应svn分支 */
    private final static int RELEASE_MINOR_VERSION_INDEX = 1;
    /** 正式发行包的修订版本号 该分支第n次修复 */
    private final static int RELEASE_PATCH_VERSION_INDEX = 2;
    /** 正式发行包的代码svn版本号 */
    private final static int RELEASE_CODE_VERSION_INDEX = 3;
    /** 正式发行包的json配置svn版本号 */
    private final static int RELEASE_JSON_VERSION_INDEX = 4;

    public final static String UNKNOWN_VERSION = "unknown";

    /**
     * 获取当前运行的版本号<br>
     * 
     * @return
     */
    public static String getVersion() {
        // 读version.txt
        try {
            String releaseVersion = getReleaseCodeVersion();
            if (StringUtils.isNotBlank(releaseVersion) && !StringUtils.equals(UNKNOWN_VERSION, releaseVersion)) {
                return releaseVersion;
            }
        } catch (Exception e) {
        }
        // 从MANIFEST.MF中读
        final boolean isFromJar = isStartupFromJar(CodeVersionHelper.class);
        if (isFromJar) {
            try {
                final InputStream resource = CodeVersionHelper.class.getClassLoader().getResourceAsStream(MANIFEST_FILE);
                if (resource != null) {
                    final Manifest manifest = new Manifest(resource);
                    final Attributes attributes = manifest.getMainAttributes();
                    return attributes.getValue(VERSION_FIELD);
                }
            } catch (Exception ignored) {
            }
        }
        // 直接从ver.txt中读
        try {
            final File file = ResourceUtil.getFile(VERSION_FILE);
            if (file.exists()) {
                String version = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                version = StringUtils.deleteWhitespace(version);
                if (StringUtils.isNotBlank(version)) {
                    return version;
                }
            }
        } catch (Exception ignored) {
        }
        return UNKNOWN_VERSION;
    }

    /**
     * 获取代码版本号<br>
     * 
     * @return
     */
    public static String getCodeVersion() {
        // 读version.txt
        try {
            String releaseVersion = getReleaseCodeVersion();
            if (StringUtils.isNotBlank(releaseVersion) && !StringUtils.equals(UNKNOWN_VERSION, releaseVersion)) {
                String[] versionParams = releaseVersion.split(".");
                if (versionParams.length > RELEASE_CODE_VERSION_INDEX) {
                    return versionParams[RELEASE_CODE_VERSION_INDEX];
                }
            }
        } catch (Exception e) {
        }
        // 从MANIFEST.MF中读
        final boolean isFromJar = isStartupFromJar(CodeVersionHelper.class);
        if (isFromJar) {
            try {
                final InputStream resource = CodeVersionHelper.class.getClassLoader().getResourceAsStream(MANIFEST_FILE);
                if (resource != null) {
                    final Manifest manifest = new Manifest(resource);
                    final Attributes attributes = manifest.getMainAttributes();
                    return attributes.getValue(VERSION_FIELD);
                }
            } catch (Exception ignored) {
            }
        }
        // 直接从ver.txt中读
        try {
            final File file = ResourceUtil.getFile(VERSION_FILE);
            if (file.exists()) {
                String version = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                version = StringUtils.deleteWhitespace(version);
                if (StringUtils.isNotBlank(version)) {
                    return version;
                }
            }
        } catch (Exception ignored) {
        }
        return UNKNOWN_VERSION;
    }

    /**
     * 获取正式包的代码版本号
     * 
     * @return
     */
    public static String getReleaseCodeVersion() {
        String releaseVersion = getReleaseVersion();
        if (StringUtils.isNotBlank(releaseVersion) && !StringUtils.equals(UNKNOWN_VERSION, releaseVersion)) {
            String[] versionParams = releaseVersion.split(".");
            if (versionParams.length > RELEASE_CODE_VERSION_INDEX) {
                return versionParams[RELEASE_CODE_VERSION_INDEX];
            }
        }
        return UNKNOWN_VERSION;
    }

    /**
     * 获取正式包的json版本号
     * 
     * @return
     */
    public static String getReleaseJsonVersion() {
        String releaseVersion = getReleaseVersion();
        if (StringUtils.isNotBlank(releaseVersion) && !StringUtils.equals(UNKNOWN_VERSION, releaseVersion)) {
            String[] versionParams = releaseVersion.split(".");
            if (versionParams.length > RELEASE_JSON_VERSION_INDEX) {
                return versionParams[RELEASE_JSON_VERSION_INDEX];
            }
        }
        return UNKNOWN_VERSION;
    }

    /**
     * 获取正式包的完整版本号<br>
     * 读取version.txt 该文件只在打正式包时更新
     * 
     * 
     * @return
     */
    public static String getReleaseVersion() {
        try {
            File releaseVersionFile = ResourceUtil.getFile(RELEASE_VERSION_FILE);
            if (releaseVersionFile != null && releaseVersionFile.exists()) {
                String version = FileUtils.readFileToString(releaseVersionFile, StandardCharsets.UTF_8);
                version = StringUtils.deleteWhitespace(version);
                return version;
            }
        } catch (Exception e) {
        }
        return UNKNOWN_VERSION;
    }

    private static <T> boolean isStartupFromJar(Class<T> clazz) {
        URL url = clazz.getResource("");
        if (url == null) {
            return false;
        }
        String protocol = url.getProtocol();
        return protocol.equals("jar");
    }

}
