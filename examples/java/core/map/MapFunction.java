@FunctionalInterface
public interface MapFunction<I, O> {
    O apply(I item, int index, DurableContext context);
}
