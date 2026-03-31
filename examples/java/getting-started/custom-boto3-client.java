public class MyHandler extends DurableHandler<Map<String, Object>, Map<String, String>> {

    @Override
    protected DurableConfig createConfiguration() {
        // Create a custom Lambda client with specific configuration
        var lambdaClientBuilder = LambdaClient.builder()
            .httpClient(ApacheHttpClient.builder()
                .maxConnections(50)
                .connectionTimeout(Duration.ofSeconds(10))
                .socketTimeout(Duration.ofSeconds(60))
                .build());

        return DurableConfig.builder()
            .withLambdaClientBuilder(lambdaClientBuilder)
            .build();
    }

    @Override
    public Map<String, String> handleRequest(Map<String, Object> event, DurableContext ctx) {
        // Your durable function logic
        return Map.of("status", "success");
    }
}
