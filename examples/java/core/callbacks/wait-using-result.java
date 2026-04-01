// Create callback
var callback = ctx.createCallback("approval_callback", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofHours(24))
        .build());

// Send callback ID to approval system
ctx.step("send_approval_request", Void.class,
    stepCtx -> {
        sendApprovalRequest(callback.callbackId(), event.get("request_details"));
        return null;
    });

// Wait for approval response
var approvalResult = callback.get();

if (approvalResult != null && Boolean.TRUE.equals(approvalResult.get("approved"))) {
    return Map.of("status", "approved", "details", approvalResult);
} else {
    return Map.of("status", "rejected");
}
