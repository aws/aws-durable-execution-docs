var orderId = (String) event.get("order_id");

// Create callback for approval
var approvalCallback = ctx.createCallback("order_approval", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofHours(48))           // 48 hours to approve
        .heartbeatTimeout(Duration.ofHours(12))  // Check every 12 hours
        .build());

// Send approval request to approval system
ctx.step("send_approval_request", Void.class, stepCtx -> {
    sendToApprovalSystem(Map.of(
        "callback_id", approvalCallback.callbackId(),
        "order_id", orderId,
        "details", event.get("order_details")));
    return null;
});

// Wait for approval
var approval = approvalCallback.get();

if (approval != null && Boolean.TRUE.equals(approval.get("approved"))) {
    // Process approved order
    return processOrder(orderId);
} else {
    // Handle rejection
    return Map.of("status", "rejected", "reason", approval.get("reason"));
}
