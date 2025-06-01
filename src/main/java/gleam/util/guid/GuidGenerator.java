package gleam.util.guid;

/**
 * 全局唯一id生成器<br>
 * 自增计数器前置<br>
 * 计数器前置 生成时需做额外的位运算 合服后可直接获取最大值取出所有服中已使用的计数器最大值<br>
 * 
 * 偏移值_长度__最大值___意义<br>
 * 63______1_____0______符号位 保持正整数<br>
 * 21-62__42___2^42-1___自增计数器<br>
 * 17-20___4____15______guid类型<br>
 * 0-16___17___131071____游戏服id<br>
 * 
 *
 * 
 * @author hdh
 *
 */
public interface GuidGenerator {
	/**
	 * 唯一id类型
	 * 
	 * @return
	 */
	int getType();

	/**
	 * 初始化计数器
	 * 
	 * @throws Exception
	 */
	void init() throws Exception;

	/**
	 * 生成唯一id<br>
	 * 使用主服id
	 * 
	 * @return
	 */
	long generateId();

	/**
	 * 生成唯一id
	 * 
	 * @param serverId
	 * @return
	 */
	long generateId(int serverId);

}
