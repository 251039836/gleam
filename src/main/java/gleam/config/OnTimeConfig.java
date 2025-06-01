package gleam.config;

/**
 * 带有上架时间的配置<br>
 * 时间未到 配置默认视为不存在
 * 
 * @author hdh
 */
public interface OnTimeConfig extends GameConfig {
	/**
	 * 上架时间<br>
	 * 时间戳 秒<br>
	 * 若为-1 则视为不会上架
	 * 
	 * @return
	 */
	int getUploadTime();
}
