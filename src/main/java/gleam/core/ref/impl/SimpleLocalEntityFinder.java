package gleam.core.ref.impl;

import gleam.core.Entity;
import gleam.core.ref.EntityManager;
import gleam.core.ref.EntityRef;
import gleam.core.ref.LocalEntityFinder;
import gleam.core.service.Context;

public class SimpleLocalEntityFinder implements LocalEntityFinder {

	private final Context context;

	public SimpleLocalEntityFinder(Context context) {
		this.context = context;
	}

	@Override
	public boolean isLocalEntity(int entityType, long entityId) {
		EntityManager<?> entityManager = context.getEntityManager(entityType);
		if (entityManager == null) {
			return false;
		}
		return entityManager.isLocalEntity(entityId);
	}

	@Override
	public EntityRef getLocalRef(int entityType, long entityId) {
		EntityManager<?> entityManager = context.getEntityManager(entityType);
		if (entityManager == null) {
			return null;
		}
		Entity<?> entity = entityManager.getEntity(entityId);
		if (entity == null) {
			EmptyEntityRef ref = entityManager.getEmptyEntityRef();
			return ref;
		}
		// FIXME 池?
		LocalEntityRef ref = new LocalEntityRef();
		ref.setType(entityType);
		ref.setId(entityId);
		ref.setEntity(entity);
		return ref;
	}

	@Override
	public Entity<?> getLocalEntity(int entityType, long entityId) {
		EntityManager<?> entityManager = context.getEntityManager(entityType);
		if (entityManager == null) {
			return null;
		}
		Entity<?> entity = entityManager.getEntity(entityId);
		return entity;
	}

	public Context getContext() {
		return context;
	}

}
