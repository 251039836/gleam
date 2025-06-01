package gleam.communication.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import gleam.communication.Connection;
import gleam.communication.authenticate.Identity;
import gleam.communication.define.DisconnectReason;

/**
 * 链接管理类<br>
 * 
 * @author hdh
 *
 */
public class ConnectionManager {

    /**
     * 所有链接<br>
     * 含未成功注册的链接 channelId,connection
     */
    protected final ConcurrentMap<String, Connection> connectionMap = new ConcurrentHashMap<>();
    /**
     * 已注册的链接<br>
     * identity,connection
     */
    protected final ConcurrentMap<Identity, Connection> identityConnectionMap = new ConcurrentHashMap<>();

    /**
     * 添加链接
     * 
     * @param connection
     */
    public void addConnection(Connection connection) {
        String uid = connection.getId();
        connectionMap.put(uid, connection);
        Identity identity = connection.getIdentity();
        if (identity != null) {
            Connection oldConnection = identityConnectionMap.put(identity, connection);
            if (oldConnection != null && StringUtils.equals(oldConnection.getId(), uid)) {
                connection.close(DisconnectReason.SAME_IDENTITY);
            }
        }
    }

    /**
     * 给链接绑定身份<br>
     * 
     * @param identity
     * @param connection
     * @return 同身份的不同连接
     */
    public Connection bindingIdentity(Identity identity, Connection connection) {
        Identity oldIdentity = connection.getIdentity();
        if (oldIdentity != null) {
            identityConnectionMap.remove(oldIdentity);
        }
        connection.setIdentity(identity);
        connectionMap.put(connection.getId(), connection);
        Connection oldConnection = identityConnectionMap.put(identity, connection);
        if (oldConnection != null && !StringUtils.equals(connection.getId(), oldConnection.getId())) {
            return oldConnection;
        }
        return null;
    }

    public Connection getConnection(Identity identity) {
        return identityConnectionMap.get(identity);
    }

    public Connection getConnection(String uid) {
        return connectionMap.get(uid);
    }

    public ConcurrentMap<String, Connection> getConnectionMap() {
        return connectionMap;
    }

    public int getConnectionSize() {
        return connectionMap.size();
    }

    public ConcurrentMap<Identity, Connection> getIdentityConnectionMap() {
        return identityConnectionMap;
    }

    public int getIdentityConnectionSize() {
        return identityConnectionMap.size();
    }

    /**
     * 移除链接
     * 
     * @param connection
     */
    public void removeConnection(Connection connection) {
        String uid = connection.getId();
        connectionMap.remove(uid);
        Identity identity = connection.getIdentity();
        if (identity != null) {
            identityConnectionMap.remove(identity, connection);
        }
    }

    /**
     * 移除链接
     * 
     * @param identity
     */
    public void removeConnection(Identity identity) {
        Connection connection = identityConnectionMap.remove(identity);
        if (connection != null) {
            connectionMap.remove(connection.getId());
        }
    }

}
