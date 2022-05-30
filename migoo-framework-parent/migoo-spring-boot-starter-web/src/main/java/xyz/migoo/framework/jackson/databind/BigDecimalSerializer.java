package xyz.migoo.framework.jackson.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

import static java.math.RoundingMode.HALF_DOWN;
import static xyz.migoo.framework.common.enums.NumberConstants.N_2;

/**
 * @author xiaomi
 * Created on 2022/5/27 20:59
 */
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializer) throws IOException {
        if (Objects.nonNull(value)) {
            gen.writeString(value.setScale(N_2, HALF_DOWN).toString());
        }
    }
}
