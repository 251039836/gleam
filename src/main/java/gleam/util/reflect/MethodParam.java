package gleam.util.reflect;

/**
 * 参数信息, 对外API接口的形参详细信息.
 * 
 * @author Jeremy
 */
public class MethodParam {

    public static MethodParam create(String name, Class<?> paramType) {
        return new MethodParam(name, paramType);
    }

    /**
     * 形参名
     */
    private String name;

    /**
     * 形参类型
     */
    private Class<?> paramType;

    private MethodParam(String name, Class<?> paramType) {
        this.name = name;
        this.paramType = paramType;
    }

    public String getName() {
        return name;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParamType(Class<?> paramType) {
        this.paramType = paramType;
    }

    @Override
    public String toString() {
        return "MethodParam [name=" + name + ", paramType=" + paramType + "]";
    }

}
