private static final StepConfig PAYMENT_CONFIG = StepConfig.builder()
    .semantics(StepSemantics.AT_MOST_ONCE_PER_RETRY)
    .retryStrategy(RetryStrategies.Presets.NO_RETRY)
    .build();

private static final StepConfig IDEMPOTENT_CONFIG = StepConfig.builder()
    .retryStrategy(RetryStrategies.Presets.DEFAULT)
    .build();

context.step("charge", Receipt.class,
    ctx -> payments.charge(order), PAYMENT_CONFIG);
context.step("refund", Receipt.class,
    ctx -> payments.refund(order), PAYMENT_CONFIG);
context.step("fetch-user", User.class,
    ctx -> users.get(id), IDEMPOTENT_CONFIG);
