package xyz.migoo.framework.jackson.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static java.math.RoundingMode.HALF_DOWN;
import static xyz.migoo.framework.common.enums.NumberConstants.N_2;

/**
 * @author xiaomi
 * Created on 2022/5/27 20:59
 */
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializer) throws IOException {
        gen.writeString(Optional.ofNullable(value).orElse(BigDecimal.ZERO).setScale(N_2, HALF_DOWN).toString());
    }
}
