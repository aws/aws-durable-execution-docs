import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.exception.SerDesException;
import software.amazon.lambda.durable.serde.SerDes;

public class OrderSerDes implements SerDes {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new SerDesException("Serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(String data, TypeToken<T> typeToken) {
        try {
            return mapper.readValue(data, mapper.getTypeFactory().constructType(typeToken.getType()));
        } catch (Exception e) {
            throw new SerDesException("Deserialization failed", e);
        }
    }
}
