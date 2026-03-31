public class MyHandler extends DurableHandler<Map<String, Object>, Object> {

    @Override
    public Object handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Step 1: Call external API
        var data = ctx.step("fetch-data", Map.class, stepCtx -> fetchData(event.get("id")));

        // Step 2: Wait 30 seconds
        ctx.wait("cooldown", Duration.ofSeconds(30));

        // Step 3: Process the data
        var result = ctx.step("process-data", Object.class, stepCtx -> processData(data));

        return result;
    }
}
