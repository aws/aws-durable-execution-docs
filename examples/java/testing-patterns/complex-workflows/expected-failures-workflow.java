// Expected failures workflow handler
public class ValidationHandler extends DurableHandler<Map, Integer> {
    @Override
    public Integer handleRequest(Map input, DurableContext ctx) {
        int value = (int) input.getOrDefault("value", 0);

        int validated = ctx.step("validate", Integer.class, stepCtx -> {
            if (value < 0) {
                throw new IllegalArgumentException("Value must be non-negative");
            }
            return value;
        });

        return validated;
    }
}
