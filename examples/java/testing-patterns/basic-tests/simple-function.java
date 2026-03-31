public class SimpleHandler extends DurableHandler<String, String> {
    @Override
    public String handleRequest(String input, DurableContext ctx) {
        return "Hello World!";
    }
}
