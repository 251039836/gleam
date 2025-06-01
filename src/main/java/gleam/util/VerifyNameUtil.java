package gleam.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 实名验证判断
 * 
 * @author lpg 2020年4月8日
 */
public class VerifyNameUtil {

	/**
	 * 是否成年
	 */
	public static final int ADULT_AGE = 18;

	/**
	 * 只能在20点玩 到了21点就踢
	 */
	public final static int PLAY_HOUR = 20;

	/**
	 * 只能在5 6 日玩
	 */
	public final static int[] PLAY_DAYS_OF_WEEK = { DayOfWeek.FRIDAY.getValue(), //
			DayOfWeek.SATURDAY.getValue(), DayOfWeek.SUNDAY.getValue() };

	/**
	 * 名字的正则
	 */
	private static String namePattern = "^[\\u4e00-\\u9fa5]{2,6}$";

	/**
	 * 身份证的正则
	 */
	private static String idNoPattern = "^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";

	/**
	 * 生日的的正则
	 */
	private static String birthPattern = "^(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)";

	/**
	 * 年-月-日
	 */
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	/**
	 * 名字正则
	 *
	 * @param name
	 * @return
	 */
	public static boolean fitNamePattern(String name) {
		return Pattern.matches(namePattern, name);
	}

	/**
	 * 身份证的正则
	 *
	 * @param idNo
	 * @return
	 */
	public static boolean fitIdPattern(String idNo) {
		return Pattern.matches(idNoPattern, idNo);
	}

	/**
	 * 出生年月日
	 *
	 * @param birth
	 * @return
	 */
	public static boolean fitBirthPattern(String birth) {
		return Pattern.matches(birthPattern, birth);
	}

	/**
	 * 根据出生年月日获取年龄
	 *
	 * @param day yyyyMMdd
	 * @return
	 */
	public static int getAge(String day) {
		if (StringUtils.isBlank(day)) {
			return 0;
		}
		LocalDate today = LocalDate.now();
		LocalDate playerDate = LocalDate.parse(day, DATE_FORMATTER);
		int age = (int) ChronoUnit.YEARS.between(playerDate, today);
		return age;
	}

	/**
	 * 判定日期是否符合
	 *
	 * @param birth
	 * @return
	 */
	public static boolean isFit(String birth) {
		if (StringUtils.isBlank(birth)) {
			return false;
		}
		if (birth.length() != 8) {
			return false;
		}
		if (!VerifyNameUtil.fitBirthPattern(birth)) {
			return false;
		}
		int year = Integer.parseInt(birth.substring(0, 4));
		int month = Integer.parseInt(birth.substring(4, 6));
		int day = Integer.parseInt(birth.substring(6, 8));
		try {
			LocalDate.of(year, month, day);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		int age = getAge(birth);
		return age > 0 && age < 100;
	}

}
