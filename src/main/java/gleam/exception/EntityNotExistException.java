package gleam.exception;

/**
 * 实体不存在
 * 
 * @author hdh
 *
 */
public class EntityNotExistException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4238489289360297371L;

	public EntityNotExistException() {
	}

	public EntityNotExistException(String msg) {
		super(msg);
	}

	public EntityNotExistException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public EntityNotExistException(Throwable cause) {
		super(cause);
	}

}
