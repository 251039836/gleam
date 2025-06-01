package gleam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StringUtil {

    /**
     * 过滤出英文字母<br>
     * A-Za-z
     * 
     * @param alph
     * @return
     */
    public static String filterEnglish(String alph) {
        alph = alph.replaceAll("[^(A-Za-z)]", "");
        return alph;
    }

    /**
     * 过滤出数字
     * 
     * @param number
     * @return
     */
    public static String filterNumber(String number) {
        number = number.replaceAll("[^(0-9)]", "");
        return number;
    }

    /**
     * 首字母大写
     * 
     * @param src
     * @return
     */
    public static String firstCharUpper(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }
        char ch = src.charAt(0);
        if (Character.isUpperCase(ch)) {
            // 首字母原本就是大写
            return src;
        }
        char[] charArray = src.toCharArray();
        charArray[0] = Character.toUpperCase(charArray[0]);
        return String.valueOf(charArray);
    }

    /**
     * 首字母小写
     * 
     * @param src
     * @return
     */
    public static String firstCharLower(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }
        char ch = src.charAt(0);
        if (Character.isLowerCase(ch)) {
            // 首字母原本就是小写
            return src;
        }
        char[] charArray = src.toCharArray();
        charArray[0] = Character.toLowerCase(charArray[0]);
        return String.valueOf(charArray);
    }

    /**
     * 将驼峰式命名的字符串转换为下划线大写方式。<br>
     * 如果转换前的驼峰式命名的字符串为空，则返回空字符串。 例如：HelloWorld->HELLO_WORLD
     * 
     * @param src 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String camel2UnderscoreUpper(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }
        char[] charArray = src.toCharArray();
        StringBuilder result = new StringBuilder();
        // 将第一个字符处理成大写
        result.append(Character.toUpperCase(charArray[0]));
        // 循环处理其余字符
        for (int i = 1; i < charArray.length; i++) {
            char ch = charArray[i];
            // 在非数字的 大写字母前添加下划线
            if (!Character.isDigit(ch) && Character.isUpperCase(ch)) {
                result.append("_");
            }
            // 其他字符直接转成大写
            result.append(Character.toUpperCase(ch));
        }
        return result.toString();
    }

    /**
     * 将驼峰式命名的字符串转换为下划线小写方式。<br>
     * 如果转换前的驼峰式命名的字符串为空，则返回空字符串。 例如：HelloWorld->hello_world
     * 
     * @param src 转换前的驼峰式命名的字符串
     * @return 转换后下划线小写方式命名的字符串
     */
    public static String camel2UnderscoreLower(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }
        char[] charArray = src.toCharArray();
        StringBuilder result = new StringBuilder();
        // 将第一个字符处理成小写
        result.append(Character.toLowerCase(charArray[0]));
        // 循环处理其余字符
        for (int i = 1; i < charArray.length; i++) {
            char ch = charArray[i];
            // 在非数字的 大写字母前添加下划线
            if (!Character.isDigit(ch) && Character.isUpperCase(ch)) {
                result.append("_");
            }
            // 其他字符直接转成小写
            result.append(Character.toLowerCase(ch));
        }
        return result.toString();
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。<br>
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：HELLO_WORLD->HelloWorld
     * 
     * @param src 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String underscore2camel(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }
        if (!src.contains("_")) {
            // 不含下划线，仅将首字母小写
            return firstCharLower(src);
        }
        StringBuilder result = new StringBuilder();
        // 用下划线将原始字符串分割
        String camels[] = src.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel.toLowerCase());
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 获取名称长度<br>
     * 现所有类型的字符 都只视为1长度 20201210
     * 
     * @param name
     * @return
     */
    public static int getNameLength(String name) {
        if (name == null) {
            return 0;
        }
        return name.length();
//        int length = 0;
//        for (int i = 0; i < name.length(); i++) {
//            char c = name.charAt(i);
//            if (isBasic(c) || isThai(c)) {
//                // 泰文也只计算1个字节
//                length++;
//            } else {
//                length += 2;
//            }
//        }
//        return length;
    }

    /**
     * 该字符是否基础字符(含英文)<br>
     * 0000-007F：C0控制符及基本拉丁文 (C0 Control and Basic Latin)<br>
     * 0080-00FF：C1控制符及拉丁文补充-1 (C1 Control and Latin 1 Supplement)
     * 
     * @param c
     * @return
     */
    public static boolean isBasic(char c) {
        return (c >= 0x0000 && c <= 0x00ff);
    }

    /**
     * 是否中文字符
     * 
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS//
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS//
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A//
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B) {
            return true;
        }
        return false;
    }

    /**
     * 是否英文字符
     * 
     * @param c
     * @return
     */
    public static boolean isEnglish(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    /**
     * 该字符是否泰文
     * 
     * @param c
     * @return
     */
    public static boolean isThai(char c) {
        return (c >= 0x0e00 && c <= 0x0e7f);
    }

    /**
     * map转为str<br>
     * 
     * 
     * @param map
     * @param split1 多个kv之间的连接符
     * @param split2 keyValue之间的连接符
     * @return
     */
    public static String map2Str(Map<Integer, Integer> map, String split1, String split2) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (sb.length() > 0) {
                sb.append(split1);
            }
            sb.append(key).append(split2).append(value);
        }
        return sb.toString();
    }

    /**
     * 切割字符串
     * 
     * @param str
     * @param split
     * @return
     */
    public static String[] split(String str, String split) {
        if (str == null || str.isEmpty()) {
            return new String[0];
        }
        String[] strArray = str.split(split);
        return strArray;
    }

    /**
     * 字符串转Map<Integer,Integer>
     * 
     * @param str
     * @param split1
     * @param split2
     * @return
     */
    public static Map<Integer, Integer> str2IIMap(String str, String split1, String split2) {
        Map<Integer, Integer> map = new HashMap<>();
        if (str == null || str.isEmpty()) {
            return map;
        }
        String[] str1s = str.split(split1);
        for (String str1 : str1s) {
            if (str1 == null) {
                continue;
            }
            String[] str2s = str1.split(split2);
            if (str2s.length < 2) {
                continue;
            }
            int key = Integer.parseInt(str2s[0]);
            int value = Integer.parseInt(str2s[1]);
            map.put(key, value);
        }
        return map;
    }

    /**
     * 字符串转int[]
     * 
     * @param str
     * @param split
     * @return
     */
    public static int[] str2IntArray(String str, String split) {
        if (str == null || str.isEmpty()) {
            return new int[0];
        }
        String[] strArray = str.split(split);
        int[] result = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            result[i] = Integer.parseInt(strArray[i]);
        }
        return result;
    }

    /**
     * 字符串转int[][]
     * 
     * @param str
     * @param split1
     * @param split2
     * @return
     */
    public static int[][] str2IntDyadicArray(String str, String split1, String split2) {
        if (str == null || str.isEmpty()) {
            return new int[0][];
        }
        String[] str1s = str.split(split1);
        int[][] result = new int[str1s.length][];
        for (int i = 0; i < str1s.length; i++) {
            String str1 = str1s[i];
            if (str1 == null) {
                result[i] = new int[0];
                continue;
            }
            String[] str2s = str1.split(split2);
            if (str2s.length <= 0) {
                result[i] = new int[0];
                continue;
            }
            int[] array2 = new int[str2s.length];
            for (int j = 0; j < str2s.length; j++) {
                String str2 = str2s[j];
                array2[j] = Integer.parseInt(str2);
            }
            result[i] = array2;
        }
        return result;
    }

    /**
     * 字符串转List<Integer>
     * 
     * @param str
     * @param split
     * @return
     */
    public static List<Integer> str2IntList(String str, String split) {
        List<Integer> result = new ArrayList<>();
        if (str == null || str.isEmpty()) {
            return result;
        }
        String[] strArray = str.split(split);
        for (String tmpStr : strArray) {
            result.add(Integer.parseInt(tmpStr));
        }
        return result;
    }
}
