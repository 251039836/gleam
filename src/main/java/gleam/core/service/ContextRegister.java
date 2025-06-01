package gleam.core.service;

/**
 * 上下文注册类<br>
 * 早于service.initialize执行<br>
 * 
 * @author hdh
 */
public interface ContextRegister<T extends Context> {

	void registerAll(T context) throws Exception;

}
