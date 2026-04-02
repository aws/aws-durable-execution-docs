import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class PassingDataWrongExample extends DurableHandler<Map<String, String>, Void> {

    private String registerUser(String email) {
        return "user-" + email;
    }

    private void sendFollowUpEmail(String userId) {
        // send email to user
    }

    @Override
    public Void handleRequest(Map<String, String> event, DurableContext context) {
        // ❌ WRONG: userId mutation is lost on replay after the wait
        AtomicReference<String> userId = new AtomicReference<>("");

        context.step("register-user", String.class, ctx -> {
            userId.set(registerUser(event.get("email"))); // ⚠️ Lost on replay!
            return userId.get();
        });

        context.wait("follow-up-delay", Duration.ofMinutes(10));

        context.step("send-follow-up-email", Void.class, ctx -> {
            sendFollowUpEmail(userId.get()); // userId is "" on replay
            return null;
        });

        return null;
    }
}
