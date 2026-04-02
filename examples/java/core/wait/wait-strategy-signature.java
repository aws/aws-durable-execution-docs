// Wait strategy — returns the delay Duration before the next polling attempt
@FunctionalInterface
public interface WaitForConditionWaitStrategy<T> {
    Duration evaluate(T state, int attempt);
}
