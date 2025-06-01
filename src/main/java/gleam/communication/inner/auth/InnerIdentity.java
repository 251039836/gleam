package gleam.communication.inner.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gleam.communication.authenticate.Identity;
import gleam.communication.authenticate.IdentityType;

/**
 * 内网服务器身份认证<br>
 * 若为逻辑服 且已合服 包含被合并的子服
 * 
 * @author hdh
 *
 */
public class InnerIdentity implements Identity {
    /**
     * 服务器id<br>
     * 主服id
     */
    private int id;
    /**
     * 子服id列表<br>
     * 不含主服id<br>
     */
    private List<Integer> childIds;
    /**
     * 该连接的服务器类型
     */
    private IdentityType type;

    public InnerIdentity(int id, IdentityType type) {
        super();
        this.id = id;
        this.type = type;
    }

    public InnerIdentity(int id, List<Integer> childIds, IdentityType type) {
        super();
        this.id = id;
        this.type = type;
        if (childIds != null && !childIds.isEmpty()) {
            this.childIds = new ArrayList<>();
            for (int childId : childIds) {
                if (childId != id) {
                    this.childIds.add(childId);
                }
            }
            Collections.sort(this.childIds);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InnerIdentity other = (InnerIdentity) obj;
        if (id != other.id)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public List<Integer> getAllIds() {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        if (childIds != null && !childIds.isEmpty()) {
            ids.addAll(childIds);
        }
        return ids;
    }

    public List<Integer> getChildIds() {
        return childIds;
    }

    public int getId() {
        return id;
    }

    @Override
    public String getKey() {
        return type.name() + "_" + id;
    }

    @Override
    public IdentityType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * 是否包含该服务器id
     * 
     * @param serverId
     * @return
     */
    public boolean isInclude(int serverId) {
        if (id == serverId) {
            return true;
        }
        if (childIds != null && !childIds.isEmpty()) {
            for (int tmpId : childIds) {
                if (tmpId == serverId) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setChildIds(List<Integer> childIds) {
        this.childIds = childIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(IdentityType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(type.name());
        sb.append('_');
        sb.append(id);
        if (childIds != null && !childIds.isEmpty()) {
            sb.append('(');
            boolean first = true;
            for (int childId : childIds) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(childId);
                first = false;
            }
            sb.append(')');
        }
        return sb.toString();
    }

}
