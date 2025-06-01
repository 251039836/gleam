package gleam.util.number;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NumberUtils {

    /**
     * 将2维数组转为Map<Integer,Integer>
     * 
     * @param dyadicArray
     * @return
     */
    public static Map<Integer, Integer> buildIIMap(int[][] dyadicArray) {
        Map<Integer, Integer> map = new HashMap<>();
        if (dyadicArray != null && dyadicArray.length > 0) {
            for (int[] array : dyadicArray) {
                if (array == null || array.length < 2) {
                    continue;
                }
                int key = array[0];
                int value = array[1];
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 计算最大公约数
     * 
     * @param x
     * @param y
     * @return
     */
    public static long findGCD(long x, long y) {
        long gcd = 0;
        while (y != 0) {
            gcd = x % y;
            x = y;
            y = gcd;
        }
        return x;
    }

    /**
     * 计算最大公约数
     * 
     * @param numbers 需要全为正数
     * @return 若无最大公约数 返回0
     */
    public static long findGCD(Set<Long> numbers) {
        if (numbers.isEmpty()) {
            return 0;
        }
        if (numbers.size() == 1) {
            return numbers.iterator().next();
        }
        long gcd = 0;
        boolean first = true;
        for (long number : numbers) {
            if (number <= 0) {
                return 0;
            }
            if (first) {
                gcd = number;
                first = false;
            } else {
                gcd = findGCD(gcd, number);
            }
        }
        return gcd;
    }

    /**
     * 获取long的左32位整数
     * 
     * @param l
     * @return
     */
    public static int getLeft(long l) {
        return (int) (l >> 32);
    }

    /**
     * 获取long的右32位整数
     * 
     * @param l
     * @return
     */
    public static int getRight(long l) {
        return (int) l;
    }

    /**
     * 合并2个int<br>
     * left<<32+right<br>
     * 
     * @param left
     * @param right
     * @return
     */
    public static long merge2Int(int left, int right) {
        long result = left;
        result = (result << 32) + right;
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Type resultType, Number value) {
        if (resultType == null) {
            String msg = value.getClass().getSimpleName() + " -> NULL";
            throw new NullPointerException(msg);
        }
        if (resultType == Date.class) {
            return (T) new Date(value.longValue());
        } else if (resultType == int.class || resultType == Integer.class) {
            return (T) Integer.valueOf(value.intValue());
        } else if (resultType == double.class || resultType == Double.class) {
            return (T) Double.valueOf(value.doubleValue());
        } else if (resultType == boolean.class || resultType == Boolean.class) {
            return (T) Boolean.valueOf(value.intValue() == 0);
        } else if (resultType == byte.class || resultType == Byte.class) {
            return (T) Byte.valueOf(value.byteValue());
        } else if (resultType == long.class || resultType == Long.class) {
            return (T) Long.valueOf(value.longValue());
        } else if (resultType == short.class || resultType == Short.class) {
            return (T) Short.valueOf(value.shortValue());
        } else if (resultType == float.class || resultType == Float.class) {
            return (T) Float.valueOf(value.floatValue());
        } else if (resultType == Number.class) {
            return (T) value;
        } else {
            String msg = value.getClass().getSimpleName() + " -> " + resultType;
            throw new IllegalArgumentException(new ClassCastException(msg));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Type resultType, String value) {
        if (resultType == null) {
            String msg = value.getClass().getSimpleName() + " -> NULL";
            throw new NullPointerException(msg);
        }

        if (value.contains(".")) {
            Double dvalue = Double.valueOf(value);
            return valueOf(resultType, dvalue);
        }

        if (resultType == int.class || resultType == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (resultType == double.class || resultType == Double.class) {
            return (T) Double.valueOf(value);
        } else if (resultType == boolean.class || resultType == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (resultType == byte.class || resultType == Byte.class) {
            return (T) Byte.valueOf(value);
        } else if (resultType == long.class || resultType == Long.class) {
            return (T) Long.valueOf(value);
        } else if (resultType == short.class || resultType == Short.class) {
            return (T) Short.valueOf(value);
        } else if (resultType == float.class || resultType == Float.class) {
            return (T) Float.valueOf(value);
        } else if (resultType == Number.class) {
            return (T) value;
        } else {
            String msg = value.getClass().getSimpleName() + " -> " + resultType;
            throw new IllegalArgumentException(new ClassCastException(msg));
        }
    }

}
