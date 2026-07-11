package xyz.migoo.framework.jackson.databind;

import cn.hutool.core.date.DatePattern;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author xiaomi
 * Created on 2022/5/27 20:59
 */
public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    public LocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        if (value != null) {
            gen.writeString(value.format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        }
    }
}