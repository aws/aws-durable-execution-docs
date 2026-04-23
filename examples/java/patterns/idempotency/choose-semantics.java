import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.config.StepSemantics;
import software.amazon.lambda.durable.retry.RetryStrategies;

// At-least-once (default).
context.step("upsert-user", User.class,
    ctx -> userStore.upsert(input.user()));

// At-most-once with retries disabled.
StepConfig critical = StepConfig.builder()
    .semantics(StepSemantics.AT_MOST_ONCE_PER_RETRY)
    .retryStrategy(RetryStrategies.Presets.NO_RETRY)
    .build();

context.step("charge-payment", Receipt.class,
    ctx -> paymentService.charge(input.amount(), input.cardToken()),
    critical);
