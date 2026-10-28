import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.serde.SerDesContext;

// SerDes interface
interface SerDesInterface {
    String serialize(Object value);

    <T> T deserialize(String data, TypeToken<T> typeToken);

    default String serialize(Object value, SerDesContext context) {
        return serialize(value);
    }

    default <T> T deserialize(String data, TypeToken<T> typeToken, SerDesContext context) {
        return deserialize(data, typeToken);
    }
}
