using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class LinearRetryStrategyExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // .NET has no built-in linear strategy. Build one with RetryStrategy.FromDelegate:
        // the delay grows by a fixed increment on each attempt, capped at maxDelay.
        var maxAttempts = 5;
        var initialDelay = TimeSpan.FromSeconds(2);
        var increment = TimeSpan.FromSeconds(3);
        var maxDelay = TimeSpan.FromSeconds(30);

        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.FromDelegate((error, attempt) =>
            {
                if (attempt >= maxAttempts)
                {
                    return RetryDecision.DoNotRetry();
                }

                // attempt is 1-based: initialDelay + increment * (attempt - 1), capped at maxDelay
                var delay = initialDelay + TimeSpan.FromTicks(increment.Ticks * (attempt - 1));
                if (delay > maxDelay)
                {
                    delay = maxDelay;
                }

                return RetryDecision.RetryAfter(delay);
            }),
        };

        return await ctx.StepAsync(
            async (_, _) => "ok",
            name: "call-external-api",
            config: config);
    }
}
