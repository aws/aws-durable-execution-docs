import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.serde.SerDes;

// SerDes interface
interface SerDesInterface {
    String serialize(Object value);

    <T> T deserialize(String data, TypeToken<T> typeToken);
}
