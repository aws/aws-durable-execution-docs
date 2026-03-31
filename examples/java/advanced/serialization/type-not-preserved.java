// Use TypeToken for generic types to preserve type information
var result = ctx.step("process-data", new TypeToken<List<String>>() {},
    stepCtx -> List.of("a", "b", "c"));

// Custom SerDes for type preservation
public class TypePreservingSerDes implements SerDes {

    @Override
    public String serialize(Object value) {
        // Implement type preservation logic
        return new ObjectMapper().writeValueAsString(value);
    }

    @Override
    public <T> T deserialize(String data, Class<T> clazz) {
        return new ObjectMapper().readValue(data, clazz);
    }

    @Override
    public <T> T deserialize(String data, TypeToken<T> typeToken) {
        return new ObjectMapper().readValue(data,
            new ObjectMapper().constructType(typeToken.getType()));
    }
}
