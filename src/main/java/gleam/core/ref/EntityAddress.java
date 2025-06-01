package gleam.core.ref;

import gleam.communication.authenticate.IdentityType;

/**
 * 实例所在地址
 * 
 * @author hdh
 */
public class EntityAddress {

	private int id;
	/**
	 * 服务器所在类型
	 */
	private IdentityType type;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public IdentityType getType() {
		return this.type;
	}

	public void setType(IdentityType type) {
		this.type = type;
	}

}
