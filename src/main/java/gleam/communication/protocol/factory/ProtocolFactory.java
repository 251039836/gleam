package gleam.communication.protocol.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Protocol;

/**
 * 协议工厂<br>
 * FIXME 有没必要每个server/client单独一个?
 * 
 * @author hdh
 *
 */
public class ProtocolFactory {

	private final static Logger logger = LoggerFactory.getLogger(ProtocolFactory.class);
	/**
	 * 协议id,该协议的无参构造器
	 */
	private final static Map<Integer, Constructor<? extends Protocol>> protocolMap = new HashMap<>();

	/**
	 * 根据协议号 获取对应的协议
	 * 
	 * @param id
	 * @return
	 */
	public final static Protocol getProtocol(int id) {
		Constructor<? extends Protocol> constructor = protocolMap.get(id);
		if (constructor == null) {
			logger.error("getProtocol error.protocol[{}] not register.", id);
			return null;
		}
		try {
			Protocol protocol = constructor.newInstance();
			return protocol;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			logger.error("getProtocol error.protocol[{}] clazz[{}] build error.", id,
					constructor.getDeclaringClass().getName(), e);
		}
		return null;
	}

	/**
	 * 注册协议
	 * 
	 * @param id
	 * @param clazz
	 */
	public static final void registerProtocol(int id, Class<? extends Protocol> clazz) {
		if (clazz == null) {
			throw new NullPointerException();
		}
		if (protocolMap.containsKey(id)) {
			Constructor<? extends Protocol> otherConstructor = protocolMap.get(id);
			Class<? extends Protocol> otherClazz = otherConstructor.getDeclaringClass();
			if (clazz.equals(otherClazz)) {
				// 重复注册相同协议
				return;
			}
			throw new RuntimeException(
					"Id[" + id + "]重复,new[" + clazz.getName() + "] old[" + otherClazz.getName() + "]");
		}
		try {
			Constructor<? extends Protocol> constructor = clazz.getConstructor();
			if (protocolMap.containsValue(constructor)) {
				int otherId = 0;
				for (Entry<Integer, Constructor<? extends Protocol>> entry : protocolMap.entrySet()) {
					Constructor<? extends Protocol> tmpConstructor = entry.getValue();
					Class<? extends Protocol> tmpClazz = tmpConstructor.getDeclaringClass();
					if (tmpClazz.equals(clazz)) {
						otherId = entry.getKey();
						break;
					}
				}
				throw new RuntimeException("协议[" + clazz.getName() + "]同时被Id[" + id + "][" + otherId + "]使用");
			}
			protocolMap.put(id, constructor);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("[" + clazz.getName() + "]缺少无参构造函数");
		}
	}
}
