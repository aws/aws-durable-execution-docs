// context.waitForCondition() — sync
<T> T waitForCondition(
    String name, Class<T> resultType,
    BiFunction<T, StepContext, WaitForConditionResult<T>> checkFunc);

<T> T waitForCondition(
    String name, Class<T> resultType,
    BiFunction<T, StepContext, WaitForConditionResult<T>> checkFunc,
    WaitForConditionConfig<T> config);

// context.waitForConditionAsync() — async
<T> DurableFuture<T> waitForConditionAsync(
    String name, Class<T> resultType,
    BiFunction<T, StepContext, WaitForConditionResult<T>> checkFunc,
    WaitForConditionConfig<T> config);

// Check function — returns WaitForConditionResult with isDone flag
BiFunction<T, StepContext, WaitForConditionResult<T>>

// WaitForConditionResult
public record WaitForConditionResult<T>(T value, boolean isDone) {
    public static <T> WaitForConditionResult<T> stopPolling(T value);
    public static <T> WaitForConditionResult<T> continuePolling(T value);
}

// Config
WaitForConditionConfig.<T>builder()
    .waitStrategy(strategy)     // optional, defaults to exponential backoff
    .initialState(initialState) // optional, defaults to null
    .serDes(serDes)             // optional
    .build();
