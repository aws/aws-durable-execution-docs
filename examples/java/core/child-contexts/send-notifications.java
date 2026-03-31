public class OrderConfirmationHandler extends DurableHandler<Map<String, Object>, Map> {

    @Override
    public Map handleRequest(Map<String, Object> event, DurableContext ctx) {
        String userId = (String) event.get("user_id");
        String message = "Order " + event.get("order_id") + " confirmed";

        var notifications = ctx.runInChildContext("order_notifications", Map.class, childCtx -> {
            var emailSent = childCtx.step("send_email", Boolean.class,
                stepCtx -> sendEmail(userId, message));
            var smsSent = childCtx.step("send_sms", Boolean.class,
                stepCtx -> sendSms(userId, message));
            var pushSent = childCtx.step("send_push", Boolean.class,
                stepCtx -> sendPushNotification(userId, message));

            return Map.of("email", emailSent, "sms", smsSent, "push", pushSent);
        });

        return Map.of("notifications_sent", notifications);
    }
}

public class ShipmentHandler extends DurableHandler<Map<String, Object>, Map> {

    @Override
    public Map handleRequest(Map<String, Object> event, DurableContext ctx) {
        String userId = (String) event.get("user_id");
        String message = "Order " + event.get("order_id") + " shipped";

        var notifications = ctx.runInChildContext("shipment_notifications", Map.class, childCtx -> {
            var emailSent = childCtx.step("send_email", Boolean.class,
                stepCtx -> sendEmail(userId, message));
            var smsSent = childCtx.step("send_sms", Boolean.class,
                stepCtx -> sendSms(userId, message));
            var pushSent = childCtx.step("send_push", Boolean.class,
                stepCtx -> sendPushNotification(userId, message));

            return Map.of("email", emailSent, "sms", smsSent, "push", pushSent);
        });

        return Map.of("notifications_sent", notifications);
    }
}
