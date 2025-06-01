package gleam.util;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 58进制工具类
 * 
 * @author hdh
 *
 */
public class Base58 {

	/**
	 * 没有0 l I O<br>
	 * 9+25+24
	 */
	private static final char[] ALPHABET = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

	private static final int size = ALPHABET.length;

	public static long decode(String str) {
		long number = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int index = ArrayUtils.indexOf(ALPHABET, c);
			number = number * size + index;
		}
		return number;
	}

	public static String encode(long number) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			int last = (int) (number % size);
			char c = ALPHABET[last];
			sb.append(c);
			if (number < size) {
				break;
			}
			number /= size;
		}
		return sb.reverse().toString();

	}

}
