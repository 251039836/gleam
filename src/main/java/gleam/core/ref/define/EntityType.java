package gleam.core.ref.define;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

/**
 * 支持远程调用的实例类型
 * 
 * @author hdh
 *
 */
public enum EntityType {
	PLAYER(1, TimeUnit.HOURS.toMillis(10), TimeUnit.HOURS.toMillis(12)),

	ALLIANCE(2, TimeUnit.MINUTES.toMillis(10), TimeUnit.HOURS.toMillis(24)),

	CROSS_ROOM(3, TimeUnit.MINUTES.toMillis(10), -1),

	;

	private int id;
	/**
	 * 无效移除时间<br>
	 * 若对应的实例不存在/获取失败 多久后才移除<br>
	 * 毫秒
	 */
	private long invalidTime;
	/**
	 * 不活跃移除时间<br>
	 * 无操作后多久会移除<br>
	 * 毫秒 -1不移除
	 */
	private long inactiveTime;

	private EntityType(int id, long invalidTime, long inactiveTime) {
		this.id = id;
		this.invalidTime = invalidTime;
		this.inactiveTime = inactiveTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getInvalidTime() {
		return invalidTime;
	}

	public void setInvalidTime(long invalidTime) {
		this.invalidTime = invalidTime;
	}

	public long getInactiveTime() {
		return inactiveTime;
	}

	public void setInactiveTime(long inactiveTime) {
		this.inactiveTime = inactiveTime;
	}

	private static final Map<Integer, EntityType> typesMap = Maps.uniqueIndex(Arrays.asList(EntityType.values()), t -> {
		assert t != null;
		return t.getId();
	});

	public static EntityType valueOf(int type) {
		return typesMap.get(type);
	}
}
