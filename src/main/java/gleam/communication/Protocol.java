package gleam.communication;

import io.netty.buffer.ByteBuf;

/**
 * 协议<br>
 * 可编解码
 * 
 * @author hdh
 *
 */
public interface Protocol {

	/**
	 * 协议id
	 * 
	 * @return
	 */
	int getId();

	/**
	 * 序号 用于标记请求对应的返回协议<br>
	 * 默认返回协议的seq=请求协议的seq(客户端)<br>
	 * 内网服务器之间通信时 rpc请求>0 返回为请求的负值 以支持双向rpc<br>
	 * 
	 * @return
	 */
	int getSeq();

	/**
	 * 
	 * @param seq
	 */
	void setSeq(int seq);

	/**
	 * 解码
	 * 
	 * @param buffer
	 * @throws Exception
	 */
	void decode(ByteBuf buffer) throws Exception;

	/**
	 * 编码
	 * 
	 * @param buffer
	 */
	void encode(ByteBuf buffer);

	/**
	 * 接收到该协议的链接<br>
	 * 仅接收协议时不为空
	 * 
	 * @return
	 */
	Connection getConnection();

	void setConnection(Connection connection);
}
