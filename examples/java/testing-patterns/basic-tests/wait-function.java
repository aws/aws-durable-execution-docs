public class WaitHandler extends DurableHandler<String, String> {
    @Override
    public String handleRequest(String input, DurableContext ctx) {
        ctx.wait("wait", Duration.ofSeconds(5));
        return "Wait completed";
    }
}
