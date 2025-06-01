package gleam.util.script;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.util.number.NumberUtils;
import groovy.lang.GroovyClassLoader;

/**
 * 脚本工具类<br>
 * 使用groovy引擎执行js代码
 * 
 * @author hdh
 *
 */
public class ScriptUtil {
    private final static Logger logger = LoggerFactory.getLogger(ScriptUtil.class);

    private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private static ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("groovy");

    static {
        try {
            scriptEngine.eval("1+1");
        } catch (Exception e) {
            logger.error("eval error");
        }
    }

    public static <T> T calculate(String formula, Map<String, Object> params, Class<T> returnType) {
        if (formula == null || formula.isEmpty()) {
            return null;
        }
        try {
            Object result = null;
            if (params == null || params.isEmpty()) {
                result = scriptEngine.eval(formula);
            } else {
                Bindings bindings = scriptEngine.createBindings();
                bindings.putAll(params);
                result = scriptEngine.eval(formula, bindings);
            }
            if (Number.class.isAssignableFrom(returnType)) {
                // 数值类型转换
                if (result instanceof Number) {
                    return NumberUtils.valueOf(returnType, (Number) result);
                }
            }
            @SuppressWarnings("unchecked")
            T t = (T) result;
            return t;
        } catch (ScriptException e) {
            e.printStackTrace();
            logger.error("formula[" + formula + "] calculateInt error.", e);
        }
        return null;
    }

    public static int calculateInt(String formula, Map<String, Object> params) {
        if (formula == null || formula.isEmpty()) {
            return 0;
        }
        try {
            Object result = null;
            if (params == null || params.isEmpty()) {
                result = scriptEngine.eval(formula);
            } else {
                Bindings bindings = scriptEngine.createBindings();
                bindings.putAll(params);
                result = scriptEngine.eval(formula, bindings);
            }
            if (result == null) {
                return 0;
            }
            if (result instanceof Number) {
                Number value = (Number) result;
                return value.intValue();
            }
            return (int) result;
        } catch (ScriptException e) {
            e.printStackTrace();
            logger.error("formula[" + formula + "] calculateInt error.", e);
        }
        return 0;
    }

    public static long calculateLong(String formula, Map<String, Object> params) {
        if (formula == null || formula.isEmpty()) {
            return 0;
        }
        try {
            Object result = null;
            if (params == null || params.isEmpty()) {
                result = scriptEngine.eval(formula);
            } else {
                Bindings bindings = scriptEngine.createBindings();
                bindings.putAll(params);
                result = scriptEngine.eval(formula, bindings);
            }
            if (result == null) {
                return 0;
            }
            if (result instanceof Number) {
                Number value = (Number) result;
                return value.longValue();
            }
            return (long) result;
        } catch (ScriptException e) {
            e.printStackTrace();
            logger.error("formula[" + formula + "] calculateInt error.", e);
        }
        return 0;
    }

    /**
     * 执行指定脚本代码 返回结果<br>
     * 内容相当于一个方法内的逻辑
     * 
     * @param code
     * @param params
     * @return
     * @throws ScriptException
     */
    public static Object executeCode(String code, Map<String, Object> params) throws ScriptException {
        if (code == null || code.isEmpty()) {
            return null;
        }
        Object result = null;
        if (params == null || params.isEmpty()) {
            result = scriptEngine.eval(code);
        } else {
            Bindings bindings = scriptEngine.createBindings();
            bindings.putAll(params);
            result = scriptEngine.eval(code, bindings);
        }
        return result;
    }

    /**
     * 执行GroovyScript接口的实现类的代码<br>
     * 内容相当于1个完整的类 必须实现GroovyScript接口
     * 
     * {@link GroovyScript}
     * 
     * @param code
     * @return
     * @throws Exception
     */
    public static Object executeGroovyScriptClass(String code) throws Exception {
        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl) scriptEngine;
        GroovyClassLoader loader = gse.getClassLoader();
        Class<?> clazz = loader.parseClass(code);
        if (clazz == null) {
            logger.error("parse script class fail.script code error.code:{}", code);
            throw new IllegalArgumentException("parse script class fail.script code error.");
        }
        if (!GroovyScript.class.isAssignableFrom(clazz)) {
            logger.error("executeGroovyScriptClass error.clazz[{}] not GroovyScript implementor", clazz.getName());
            throw new IllegalArgumentException("executeGroovyScriptClass error.not GroovyScript implementor.");
        }
        Class<? extends GroovyScript> scriptClazz = clazz.asSubclass(GroovyScript.class);
        GroovyScript script = scriptClazz.getConstructor().newInstance();
        return script.run();
    }

    public static ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

}
