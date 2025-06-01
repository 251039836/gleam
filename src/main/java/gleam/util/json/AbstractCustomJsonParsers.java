package gleam.util.json;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;

/**
 * 自定义json解析器
 * 
 * @author hdh
 * @time 2022年4月25日
 *
 * @param <T>
 */
public abstract class AbstractCustomJsonParsers<T> extends JsonDeserializer<T> implements ResolvableDeserializer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected JsonDeserializer<T> defaultDeserializer;

    public AbstractCustomJsonParsers() {
    }

    public AbstractCustomJsonParsers(JsonDeserializer<T> defaultDeserializer) {
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    public T deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return defaultDeserializer.deserialize(jp, ctxt);
    }

    public JsonDeserializer<T> getDefaultDeserializer() {
        return defaultDeserializer;
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
    }

    public void setDefaultDeserializer(JsonDeserializer<T> defaultDeserializer) {
        this.defaultDeserializer = defaultDeserializer;
    }
}
