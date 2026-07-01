using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ChooseSemanticsExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<PaymentEvent, Receipt>(Workflow, input, context);

    private async Task<Receipt> Workflow(PaymentEvent input, IDurableContext ctx)
    {
        // At-least-once (default) for a retryable idempotent write.
        await ctx.StepAsync(
            async (_, ct) => await UserStore.UpsertAsync(input.User, ct),
            name: "upsert-user");

        // At-most-once for a side-effecting call, with retries disabled.
        var critical = new StepConfig
        {
            Semantics = StepSemantics.AtMostOncePerRetry,
            RetryStrategy = RetryStrategy.None,
        };

        return await ctx.StepAsync(
            async (_, ct) => await PaymentService.ChargeAsync(input.Amount, input.CardToken, ct),
            name: "charge-payment",
            config: critical);
    }
}

public record PaymentEvent(User User, decimal Amount, string CardToken);
public record User(string Id);
public record Receipt(string TransactionId);

public static class UserStore
{
    public static Task<User> UpsertAsync(User user, CancellationToken ct) => Task.FromResult(user);
}

public static class PaymentService
{
    public static Task<Receipt> ChargeAsync(decimal amount, string cardToken, CancellationToken ct)
        => Task.FromResult(new Receipt("txn-1"));
}
