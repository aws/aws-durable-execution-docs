public class SerializeHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Use custom serialization for invoke
        InvokeConfig config = InvokeConfig.builder()
            .payloadSerDes(new CustomSerDes())
            .serDes(new CustomSerDes())
            .build();

        var result = ctx.invoke(
            "custom_invoke",
            "custom-function",
            Map.of("complex", "data"),
            Map.class,
            config
        );

        return result;
    }
}
