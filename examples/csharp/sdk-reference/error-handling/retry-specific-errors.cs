using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class RetrySpecificErrorsExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // Pass retryableExceptions to retry only these exception types (and subclasses).
        // Every other exception fails immediately.
        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.Exponential(
                maxAttempts: 5,
                initialDelay: TimeSpan.FromSeconds(2),
                maxDelay: TimeSpan.FromMinutes(1),
                backoffRate: 2.0,
                jitter: JitterStrategy.Full,
                retryableExceptions: new[]
                {
                    typeof(RateLimitException),
                    typeof(ServiceUnavailableException),
                }),
        };

        return await ctx.StepAsync(
            async (_, _) => CallApi(),
            name: "call-api",
            config: config);
    }

    private static string CallApi() => "ok";
}

public class RateLimitException : Exception
{
    public RateLimitException(string message) : base(message) { }
}

public class ServiceUnavailableException : Exception
{
    public ServiceUnavailableException(string message) : base(message) { }
}
