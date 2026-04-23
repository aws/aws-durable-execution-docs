import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import java.util.UUID;

public class ChargeHandler implements DurableHandler<ChargeInput, Receipt> {
    @Override
    public Receipt handle(ChargeInput input, DurableContext context) {
        String transactionId = context.step(
            "generate-transaction-id",
            String.class,
            ctx -> UUID.randomUUID().toString());

        return context.step(
            "charge",
            Receipt.class,
            ctx -> paymentService.charge(input.amount(), transactionId));
    }
}
