public class CompressedSerDes implements SerDes {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(Object value) {
        // In production, use actual compression like gzip
        return mapper.writeValueAsString(value);
    }

    @Override
    public <T> T deserialize(String data, Class<T> clazz) {
        return mapper.readValue(data, clazz);
    }

    @Override
    public <T> T deserialize(String data, TypeToken<T> typeToken) {
        return mapper.readValue(data, mapper.constructType(typeToken.getType()));
    }
}

// Usage: custom SerDes for a specific step
var largeData = Map.of("items", IntStream.range(0, 1000)
    .mapToObj(i -> "item_" + i)
    .collect(Collectors.toList()));

var config = StepConfig.builder()
    .serDes(new CompressedSerDes())
    .build();

var result = ctx.step("process-large-data", Map.class,
    stepCtx -> Map.of("processed", true, "items", largeData.get("items").size()), config);
