package gleam.core.ref.impl;

import gleam.communication.authenticate.IdentityType;
import gleam.core.ref.EntityAddress;
import gleam.core.ref.EntityAddressFinder;
import gleam.core.ref.define.EntityType;
import gleam.util.guid.GuidDefines;

public class SimpleEntityAddressFinder implements EntityAddressFinder {

	@Override
	public EntityAddress findAddress(int entityType, long entityId) {
		if (entityType == EntityType.CROSS_ROOM.getId()) {
			// TODO FIXME 跨服分组配置
			return null;
		}
		int serverId = Math.toIntExact(entityId & GuidDefines.SERVER_ID_UPPER_BOUND);
		if (serverId <= 0) {
			return null;
		}
		EntityAddress address = new EntityAddress();
		address.setId(serverId);
		address.setType(IdentityType.LOGIC);
		return address;
	}

}
