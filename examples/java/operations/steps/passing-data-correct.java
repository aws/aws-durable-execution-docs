import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class PassingDataCorrectExample extends DurableHandler<Map<String, String>, Void> {

    private String registerUser(String email) {
        return "user-" + email;
    }

    private void sendFollowUpEmail(String userId) {
        // send email to user
    }

    @Override
    public Void handleRequest(Map<String, String> event, DurableContext context) {
        // ✅ CORRECT: userId is returned from the step and restored from checkpoint on replay
        String userId = context.step("register-user", String.class,
            ctx -> registerUser(event.get("email")));

        context.wait("follow-up-delay", Duration.ofMinutes(10));

        context.step("send-follow-up-email", Void.class, ctx -> {
            sendFollowUpEmail(userId); // userId restored from checkpoint
            return null;
        });

        return null;
    }
}
