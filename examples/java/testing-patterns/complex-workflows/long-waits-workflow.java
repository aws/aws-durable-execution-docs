// Long waits workflow handler
public class LongWaitHandler extends DurableHandler<Map, String> {
    @Override
    public String handleRequest(Map input, DurableContext ctx) {
        ctx.step("start", String.class, stepCtx -> "Starting");
        ctx.wait("long_wait", Duration.ofHours(1));
        ctx.step("continue", String.class, stepCtx -> "Continuing");
        return "Complete";
    }
}
