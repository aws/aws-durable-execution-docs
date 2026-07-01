using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class HandleErrorsInStepExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<ApiEvent, Record>(Workflow, input, context);

    private async Task<Record> Workflow(ApiEvent input, IDurableContext ctx)
    {
        // Retry only transient errors. Anything else (bad input, not-found) fails immediately.
        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.Exponential(
                maxAttempts: 5,
                initialDelay: TimeSpan.FromSeconds(2),
                maxDelay: TimeSpan.FromMinutes(1),
                retryableExceptions: new[] { typeof(TransientApiException), typeof(RateLimitException) }),
        };

        return await ctx.StepAsync(
            async (_, ct) => await ExternalApi.Get(input.Id, ct),
            name: "call-api",
            config: config);
    }
}

public class TransientApiException : Exception { }
public class RateLimitException : Exception { }

public record ApiEvent(string Id);
public record Record(string Id, string Data);

public static class ExternalApi
{
    public static Task<Record> Get(string id, CancellationToken ct)
        => Task.FromResult(new Record(id, "data"));
}
