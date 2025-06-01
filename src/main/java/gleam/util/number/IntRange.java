package gleam.util.number;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * int范围<br>
 * 2个int之间包含的范围<br>
 * {@link groovy.lang.IntRange}
 * 
 * @author hdh
 *
 */
public class IntRange implements Iterable<Integer>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5818477777738391750L;

    /**
     * [minNum,maxNum]区间
     * 
     * @param num1
     * @param num2
     * @return
     */
    public static IntRange between(int num1, int num2) {
        if (num1 > num2) {
            return new IntRange(num2, num1);
        }
        return new IntRange(num1, num2);
    }

    /**
     * [num,num]的区间
     * 
     * @param num
     * @return
     */
    public static IntRange single(int num) {
        return new IntRange(num, num);
    }

    /**
     * 最小数值<br>
     * 包含该值
     */
    private final int minNum;
    /**
     * 最大数值<br>
     * 包含该值
     */
    private final int maxNum;

    public IntRange(int minNum, int maxNum) {
        super();
        this.minNum = minNum;
        this.maxNum = maxNum;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public int getMinNum() {
        return minNum;
    }

    /**
     * 该数字是否在该范围内
     * 
     * @param num
     * @return
     */
    public boolean contains(int num) {
        return num >= minNum && num <= maxNum;
    }

    public int size() {
        return maxNum - minNum + 1;
    }

    public int[] toArray() {
        int length = size();
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = minNum + i;
        }
        return array;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new IntRangeIterator();
    }

    @Override
    public String toString() {
        return "IntRange [minNum=" + minNum + ", maxNum=" + maxNum + "]";
    }

    private class IntRangeIterator implements Iterator<Integer> {

        private int index;

        private int size = size();

        private int value = minNum;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (index++ > 0) {
                value++;
            }
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
