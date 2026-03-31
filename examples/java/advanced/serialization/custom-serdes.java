// Example: Convert strings to uppercase during serialization
class UpperCaseSerDes implements SerDes {

    @Override
    public String serialize(Object value) {
        return value.toString().toUpperCase();
    }

    @Override
    public <T> T deserialize(String data, Class<T> clazz) {
        return clazz.cast(data.toLowerCase());
    }

    @Override
    public <T> T deserialize(String data, TypeToken<T> typeToken) {
        return (T) data.toLowerCase();
    }
}
