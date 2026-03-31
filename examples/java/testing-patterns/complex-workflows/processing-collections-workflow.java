// Processing collections workflow handler
public class CollectionHandler extends DurableHandler<Map, List<Integer>> {
    @Override
    public List<Integer> handleRequest(Map input, DurableContext ctx) {
        List<Integer> numbers = (List<Integer>) input.getOrDefault("numbers",
            List.of(1, 2, 3, 4, 5));

        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            int num = numbers.get(i);
            int result = ctx.step("square_" + i, Integer.class, stepCtx -> num * 2);
            results.add(result);
        }

        return results;
    }
}
