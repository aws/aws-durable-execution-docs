using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class RetryPresetsExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Results>(Workflow, input, context);

    private async Task<Results> Workflow(object input, IDurableContext ctx)
    {
        // Default: 6 attempts, 5s initial delay, 60s max, 2x backoff, full jitter.
        string result = await ctx.StepAsync(
            async (_, _) => CallApi(),
            name: "call-api",
            config: new StepConfig { RetryStrategy = RetryStrategy.Default });

        // Transient: 3 attempts, 1s initial delay, 5s max, 2x backoff, half jitter.
        string audit = await ctx.StepAsync(
            async (_, _) => WriteAuditLog(),
            name: "audit-log",
            config: new StepConfig { RetryStrategy = RetryStrategy.Transient });

        // None: 1 attempt, fail immediately on first error.
        string critical = await ctx.StepAsync(
            async (_, _) => ChargePayment(),
            name: "charge-payment",
            config: new StepConfig { RetryStrategy = RetryStrategy.None });

        return new Results(result, audit, critical);
    }

    private static string CallApi() => "ok";
    private static string WriteAuditLog() => "logged";
    private static string ChargePayment() => "charged";
}

public record Results(string Result, string Audit, string Critical);
