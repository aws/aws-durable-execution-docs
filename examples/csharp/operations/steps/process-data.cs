using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ProcessDataExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<DataEvent, Processed>(Workflow, input, context);

    private async Task<Processed> Workflow(DataEvent input, IDurableContext ctx)
    {
        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.Exponential(maxAttempts: 3),
            Semantics = StepSemantics.AtLeastOncePerRetry,
        };

        Processed result = await ctx.StepAsync(
            async (_, _) => ProcessData(input.Data),
            name: "process_data",
            config: config);
        return result;
    }

    private static Processed ProcessData(string data) => new(data, Status: "completed");
}

public record DataEvent(string Data);
public record Processed(string Data, string Status);
