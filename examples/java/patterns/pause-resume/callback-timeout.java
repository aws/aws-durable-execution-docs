import software.amazon.lambda.durable.config.CallbackConfig;
import software.amazon.lambda.durable.config.WaitForCallbackConfig;

WaitForCallbackConfig config = WaitForCallbackConfig.builder()
    .callbackConfig(CallbackConfig.builder()
        .timeout(Duration.ofHours(24))
        .build())
    .build();

Approval outcome = context.waitForCallback(
    "wait-for-approval",
    Approval.class,
    (callbackId, ctx) -> approvalsService.request(input.orderId(), callbackId),
    config);
