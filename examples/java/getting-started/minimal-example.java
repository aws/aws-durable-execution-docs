public class GreetingHandler extends DurableHandler<Map<String, String>, String> {

    @Override
    public String handleRequest(Map<String, String> event, DurableContext ctx) {
        var name = event.getOrDefault("name", "World");

        // Generate a greeting
        var greeting = ctx.step("greet-user", String.class, stepCtx -> {
            return "Hello " + name + "!";
        });

        return greeting;
    }
}
