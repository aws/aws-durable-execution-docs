public class AddNumbersHandler extends DurableHandler<Map<String, Object>, Integer> {

    @Override
    public Integer handleRequest(Map<String, Object> event, DurableContext ctx) {
        var result = ctx.step("add-numbers", Integer.class, stepCtx -> {
            return 5 + 3;
        });
        return result;
    }
}
