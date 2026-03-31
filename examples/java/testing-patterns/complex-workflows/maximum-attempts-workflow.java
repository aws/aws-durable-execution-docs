// Maximum attempts workflow handler
public class MaxAttemptsHandler extends DurableHandler<Map, Map> {
    @Override
    public Map handleRequest(Map input, DurableContext ctx) {
        int target = (int) input.getOrDefault("target", 10);
        int[] state = {0};
        int[] attempt = {0};
        int maxAttempts = 5;

        while (attempt[0] < maxAttempts && state[0] < target) {
            attempt[0]++;
            int currentState = state[0];
            int currentAttempt = attempt[0];
            state[0] = ctx.step("attempt_" + currentAttempt, Integer.class,
                stepCtx -> currentState + 1);

            if (state[0] < target) {
                ctx.wait("wait_" + currentAttempt, Duration.ofSeconds(1));
            }
        }

        return Map.of(
            "state", state[0],
            "attempts", attempt[0],
            "reached_target", state[0] >= target
        );
    }
}
