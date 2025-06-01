package gleam.core.ref;

import gleam.core.Entity;
import gleam.core.ref.impl.EmptyEntityRef;
import gleam.core.service.AbstractService;

public abstract class EntityManager<T extends Entity<?>> extends AbstractService {

	protected final EmptyEntityRef emptyEntityRef = buildEmptyEntityRef();

	public abstract int getEntityType();

	public abstract T getEntity(long id);

	/**
	 * 判断该id对应的实体是否应该是本地实体<br>
	 * 该id实体应当在该服 但还未有对应实体时 也返回true
	 * 
	 * @param id
	 * @return
	 */
	public abstract boolean isLocalEntity(long id);

	/**
	 * 实例不存在的错误码
	 * 
	 * @return
	 */
	protected abstract int notExistErrorCode();

	protected EmptyEntityRef buildEmptyEntityRef() {
		EmptyEntityRef emptyRef = new EmptyEntityRef(notExistErrorCode());
		return emptyRef;
	}

	public EmptyEntityRef getEmptyEntityRef() {
		return emptyEntityRef;
	}
}
