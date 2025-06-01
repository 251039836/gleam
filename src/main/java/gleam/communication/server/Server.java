package gleam.communication.server;

import java.net.InetSocketAddress;

import gleam.core.service.Service;

/**
 * 服务端
 * 
 * @author hdh
 *
 */
public interface Server extends Service {

	/**
	 * 接收器
	 * 
	 * @return
	 */
	Acceptor getAcceptor();

	/**
	 * 要监听的地址
	 * 
	 * @return
	 */
	InetSocketAddress getAddress();

	/**
	 * 链接管理类
	 * 
	 * @return
	 */
	ConnectionManager getConnectionManager();

}
