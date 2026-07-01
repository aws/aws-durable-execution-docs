using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class WithRetryHelperExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, string>(Workflow, input, context);

    private async Task<string> Workflow(OrderEvent input, IDurableContext ctx)
    {
        // .NET has no separate withRetry helper. InvokeConfig does not accept a retry
        // strategy, so wrap the invoke in a step and set the retry strategy on StepConfig.
        // The step retries the invocation with backoff between failed attempts.
        var config = new StepConfig
        {
            RetryStrategy = RetryStrategy.Exponential(
                maxAttempts: 3,
                initialDelay: TimeSpan.FromSeconds(2),
                maxDelay: TimeSpan.FromMinutes(1),
                backoffRate: 2.0,
                jitter: JitterStrategy.Full),
        };

        return await ctx.StepAsync(
            async (_, ct) => await ctx.InvokeAsync<PaymentRequest, string>(
                "process-payment",
                new PaymentRequest(input.OrderId),
                name: "charge",
                cancellationToken: ct),
            name: "charge-payment",
            config: config);
    }
}

public record OrderEvent(string OrderId);
public record PaymentRequest(string OrderId);
