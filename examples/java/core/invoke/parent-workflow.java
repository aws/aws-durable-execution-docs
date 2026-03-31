public class ParentWorkflowHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        String projectId = (String) event.get("project_id");

        // Invoke sub-workflow for data collection
        var data = ctx.invoke(
            "collect_data",
            "collect-data-workflow",
            Map.of("project_id", projectId),
            Map.class
        );

        // Invoke sub-workflow for data processing
        var processed = ctx.invoke(
            "process_data",
            "process-data-workflow",
            data,
            Map.class
        );

        // Invoke sub-workflow for reporting
        var report = ctx.invoke(
            "generate_report",
            "generate-report-workflow",
            processed,
            Map.class
        );

        return report;
    }
}
