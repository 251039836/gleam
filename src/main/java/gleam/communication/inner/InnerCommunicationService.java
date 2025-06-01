package gleam.communication.inner;

import java.util.List;

import gleam.communication.Connection;
import gleam.communication.authenticate.IdentityType;
import gleam.core.service.Service;

/**
 * 内网通信服务
 * 
 * @author hdh
 *
 */
public interface InnerCommunicationService extends Service {

	/**
	 * 另一端的身份类型
	 * 
	 * @return
	 */
	IdentityType getRemoteType();

	/**
	 * 获取该服务器的链接
	 * 
	 * @param serverId
	 * @return
	 */
	Connection getConnection(int serverId);

	/**
	 * 获取所有有身份标识的链接
	 * 
	 * @return
	 */
	List<Connection> getAllIdentityConnections();

}
