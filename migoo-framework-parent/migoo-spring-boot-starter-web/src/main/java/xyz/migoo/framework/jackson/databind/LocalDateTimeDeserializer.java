package xyz.migoo.framework.jackson.databind;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author xiaomi
 * Created on 2022/5/27 20:59
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
        String dateString = p.getString();
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        return LocalDateTime.parse(dateString, FORMATTER);
    }
}