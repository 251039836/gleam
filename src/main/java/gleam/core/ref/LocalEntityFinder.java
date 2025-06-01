package gleam.core.ref;

import gleam.core.Entity;
import gleam.core.ref.impl.EmptyEntityRef;
import gleam.core.ref.impl.LocalEntityRef;

/**
 * 本地实体查找器
 * 
 * @author hdh
 *
 */
public interface LocalEntityFinder {

	/**
	 * 该实体是否应该是本地实体<br>
	 * 对应实体可能还未创建 若生成应当时是本地实体时也返回true
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	boolean isLocalEntity(int entityType, long entityId);

	/**
	 * 获取本地实体对象
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	Entity<?> getLocalEntity(int entityType, long entityId);

	/**
	 * 获取本地实体的引用<br>
	 * 若实体存在 则返回{@link LocalEntityRef}<br>
	 * 若该实体应该是本地实体 但还未有对应实体 则返回{@link EmptyEntityRef}
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	EntityRef getLocalRef(int entityType, long entityId);

}
