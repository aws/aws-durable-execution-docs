public class MyHandler extends DurableHandler<Map<String, Object>, Object> {

    @Override
    public Object handleRequest(Map<String, Object> event, DurableContext ctx) {
        var result = ctx.step("my-step", Object.class, stepCtx -> {
            // Your business logic
            return processData(event.get("data"));
        });

        return result;
    }
}
