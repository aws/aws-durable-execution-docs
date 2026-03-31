// Wait for condition workflow handler
public class WaitForConditionHandler extends DurableHandler<Map, Integer> {
    @Override
    public Integer handleRequest(Map input, DurableContext ctx) {
        int[] state = {0};
        int[] attempt = {0};
        int maxAttempts = 5;

        while (attempt[0] < maxAttempts) {
            attempt[0]++;
            int currentState = state[0];
            int currentAttempt = attempt[0];
            state[0] = ctx.step("increment_" + currentAttempt, Integer.class,
                stepCtx -> currentState + 1);

            if (state[0] >= 3) {
                break;
            }

            ctx.wait("wait_" + currentAttempt, Duration.ofSeconds(1));
        }

        return state[0];
    }
}
