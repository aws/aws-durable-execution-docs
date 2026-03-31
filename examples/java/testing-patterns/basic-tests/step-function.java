public class StepHandler extends DurableHandler<Map, Integer> {
    @Override
    public Integer handleRequest(Map input, DurableContext ctx) {
        int result = ctx.step("add_numbers", Integer.class, stepCtx -> 5 + 3);
        return result;
    }
}
