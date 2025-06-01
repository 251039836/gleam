package gleam.core.ref.define;

public class EntityRefState {
	/**
	 * 初始状态
	 */
	public final static int INIT = 1;
	/**
	 * 正在判断对应实体是否存在
	 */
	public final static int CHECK = 2;
	/**
	 * 实体存在 可运行<br>
	 */
	public final static int RUN = 3;
	/**
	 * 无效<br>
	 * 寻址后确定实体不存在/查找超时
	 */
	public final static int INVALID = 4;

}
