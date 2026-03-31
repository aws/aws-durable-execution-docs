public class InvokeOnDemandFunctionHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Invoke a mix of durable and on-demand functions
        String userId = (String) event.get("user_id");

        // Invoke a regular Lambda function for data fetching
        var userData = ctx.invoke(
            "fetch_user",
            "fetch-user-data",
            Map.of("user_id", userId),
            Map.class
        );

        // Invoke a durable function for complex processing
        var processed = ctx.invoke(
            "process_user",
            "process-user-workflow",
            userData,
            Map.class
        );

        // Invoke another regular Lambda for notifications
        var notification = ctx.invoke(
            "send_notification",
            "send-notification",
            Map.of("user_id", userId, "data", processed),
            Map.class
        );

        return Map.of(
            "status", "completed",
            "notification_sent", notification.get("sent")
        );
    }
}
