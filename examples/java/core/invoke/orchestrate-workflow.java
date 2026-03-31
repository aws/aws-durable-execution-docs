public class OrchestrateWorkflowHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        String userId = (String) event.get("user_id");

        // Step 1: Fetch user data
        var user = ctx.invoke(
            "fetch_user",
            "fetch-user",
            Map.of("user_id", userId),
            Map.class
        );

        // Step 2: Enrich user data
        var enrichedUser = ctx.invoke(
            "enrich_user",
            "enrich-user-data",
            user,
            Map.class
        );

        // Step 3: Generate report
        var report = ctx.invoke(
            "generate_report",
            "generate-report",
            enrichedUser,
            Map.class
        );

        return report;
    }
}
