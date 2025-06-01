package gleam.util.serverid;

/**
 * serverId 组<br>
 * [[id1],[id2,id3]]<br>
 * 相关服务器id1或服务器id段[id2,id3]
 * 
 * @author hdh
 */
public class ServerIdGroup {

	protected int[][] serverIds;

	public ServerIdGroup() {
	}

	public ServerIdGroup(int[][] serverIds) {
		super();
		this.serverIds = serverIds;
	}

	public int[][] getServerIds() {
		return serverIds;
	}

	public void setServerIds(int[][] serverIds) {
		this.serverIds = serverIds;
	}

	public boolean isEmpty() {
		if (serverIds == null) {
			return true;
		}
		if (serverIds.length == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 该服务器id是否在范围内
	 * 
	 * @param serverId
	 * @return
	 */
	public boolean isFitServer(int serverId) {
		if (serverIds == null) {
			return false;
		}
		for (int[] is : serverIds) {
			if (is.length == 1) {
				if (serverId == is[0]) {
					return true;
				}
			}
			if (is.length == 2) {
				if (serverId >= is[0] && serverId <= is[1]) {
					return true;
				}
			}
		}
		return false;
	}
}
