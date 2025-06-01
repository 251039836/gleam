package gleam.util;

import java.util.Locale;

/**
 * 操作系统工具类
 * 
 * @author hdh
 *
 */
public class OsUtil {

    private static final String OS_WINDOWS = "windows";
    private static final String OS_LINUX = "linux";
    private static final String OS_MAC = "mac";

    public static String getOsArch() {
        return System.getProperty("os.arch").toLowerCase(Locale.US);
    }

    public static String getOsName() {
        return System.getProperty("os.name").toLowerCase(Locale.US);
    }

    public static String getOsVersion() {
        return System.getProperty("os.version").toLowerCase(Locale.US);
    }

    /**
     * 是否linux系统
     * 
     * @return
     */
    public static boolean isLinux() {
        return getOsName().contains(OS_LINUX);
    }

    /**
     * 是否mac系统
     * 
     * @return
     */
    public static boolean isMac() {
        return getOsName().contains(OS_MAC);
    }

    /**
     * 是否windows系统
     * 
     * @return
     */
    public static boolean isWindows() {
        return getOsName().contains(OS_WINDOWS);
    }
}
