package gleam.core.ref;

/**
 * 实体地址查找器
 * 
 * @author hdh
 *
 */
public interface EntityAddressFinder {

	EntityAddress findAddress(int entityType, long entityId);
}
