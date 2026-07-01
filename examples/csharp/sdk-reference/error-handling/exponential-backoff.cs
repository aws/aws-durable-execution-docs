using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ExponentialBackoffExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.Exponential(
                maxAttempts: 3,
                initialDelay: TimeSpan.FromSeconds(1),
                maxDelay: TimeSpan.FromSeconds(10),
                backoffRate: 2.0,
                jitter: JitterStrategy.Full),
        };

        string result = await ctx.StepAsync(
            async (_, _) => "Step with exponential backoff",
            name: "retry_step",
            config: config);

        return $"Result: {result}";
    }
}
