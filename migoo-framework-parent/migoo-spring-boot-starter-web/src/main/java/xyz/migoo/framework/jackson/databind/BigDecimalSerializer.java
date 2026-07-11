package xyz.migoo.framework.jackson.databind;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.RoundingMode.HALF_DOWN;
import static xyz.migoo.framework.common.enums.NumberConstants.N_2;

/**
 * @author xiaomi
 * Created on 2022/5/27 20:59
 */
public class BigDecimalSerializer extends StdSerializer<BigDecimal> {
    public BigDecimalSerializer() {
        super(BigDecimal.class);
    }


    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        gen.writeString(Optional.ofNullable(value).orElse(BigDecimal.ZERO).setScale(N_2, HALF_DOWN).toString());
    }
}