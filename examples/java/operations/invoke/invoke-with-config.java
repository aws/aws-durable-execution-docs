import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.InvokeConfig;

public class InvokeWithConfig extends DurableHandler<OrderEvent, OrderResult> {

    @Override
    public OrderResult handle(OrderEvent event, DurableContext context) {
        InvokeConfig config = InvokeConfig.builder()
            .tenantId(event.getTenantId())
            .build();

        return context.invoke(
            "process-order",
            "order-processor-function:live",
            event,
            OrderResult.class,
            config
        );
    }
}
