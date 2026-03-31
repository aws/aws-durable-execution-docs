// Partial failures workflow handler
public class PartialFailureHandler extends DurableHandler<Map, String> {
    @Override
    public String handleRequest(Map input, DurableContext ctx) {
        ctx.step("step1", String.class, stepCtx -> "Step 1 complete");
        ctx.step("step2", String.class, stepCtx -> "Step 2 complete");
        ctx.step("step3", String.class, stepCtx -> {
            throw new RuntimeException("Step 3 failed");
        });
        return "Should not reach here";
    }
}
