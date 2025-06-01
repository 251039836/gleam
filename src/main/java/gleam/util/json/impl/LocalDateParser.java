package gleam.util.json.impl;

import java.io.IOException;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalDateParser extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String dateStr = p.getValueAsString();
        LocalDate localDate = parseLocalDate(dateStr);
        return localDate;
    }

    private LocalDate parseLocalDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateStr);
        return date;
    }
}
