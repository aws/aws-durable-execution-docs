public class InvokeServiceHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Fan out to multiple services
        String[] services = {"service-a", "service-b", "service-c"};

        List<Object> results = new ArrayList<>();
        for (String service : services) {
            var result = ctx.invoke(
                "invoke_" + service,
                service,
                event,
                Map.class
            );
            results.add(result);
        }

        return Map.of("results", results);
    }
}
