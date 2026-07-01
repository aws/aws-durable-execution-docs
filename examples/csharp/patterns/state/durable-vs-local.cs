using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class DurableVsLocalExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Result>(Workflow, input, context);

    private async Task<Result> Workflow(object input, IDurableContext ctx)
    {
        // The step return value is checkpointed as durable state.
        Value value = await ctx.StepAsync(async (_, _) => FetchValue(), name: "fetch");

        // Assigning or copying the local variable does NOT add to durable state.
        Value alias = value;
        Value copy = value with { }; // record copy

        // Returning the local variable from the handler DOES add it to durable state
        // (the handler output is serialized and stored).
        return new Result(value);
    }

    private static Value FetchValue() => new Value("payload");
}

public record Value(string Data);
public record Result(Value Value);
