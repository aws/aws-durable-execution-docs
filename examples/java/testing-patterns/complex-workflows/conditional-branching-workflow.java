// Conditional branching workflow handler
public class ConditionalHandler extends DurableHandler<Map, String> {
    @Override
    public String handleRequest(Map input, DurableContext ctx) {
        int amount = (int) input.getOrDefault("amount", 0);

        ctx.step("validate_amount", Integer.class, stepCtx -> amount);

        String result;
        if (amount > 1000) {
            ctx.step("approval", String.class, stepCtx -> "Manager approval required");
            ctx.wait("approval_wait", Duration.ofSeconds(10));
            result = ctx.step("process_high", String.class,
                stepCtx -> "High-value order processed");
        } else {
            result = ctx.step("process_standard", String.class,
                stepCtx -> "Standard order processed");
        }

        return result;
    }
}
