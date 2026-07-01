using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class CustomRetryStrategyExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    // RetryStrategy.FromDelegate takes (Exception error, int attempt) => RetryDecision.
    // attempt is 1-based: 1 on the first retry, 2 on the second, etc.
    private static readonly IRetryStrategy CustomStrategy = RetryStrategy.FromDelegate((error, attempt) =>
    {
        if (attempt >= 4)
        {
            return RetryDecision.DoNotRetry();
        }

        // Fixed 2-second delay regardless of attempt number.
        return RetryDecision.RetryAfter(TimeSpan.FromSeconds(2));
    });

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        var config = new StepConfig { RetryStrategy = CustomStrategy };

        return await ctx.StepAsync(
            async (_, _) => CallApi(),
            name: "call-api",
            config: config);
    }

    private static string CallApi() => "ok";
}
