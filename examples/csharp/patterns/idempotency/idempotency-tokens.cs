using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class IdempotencyTokensExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<PaymentEvent, Receipt>(Workflow, input, context);

    private async Task<Receipt> Workflow(PaymentEvent input, IDurableContext ctx)
    {
        // Derive a stable idempotency key inside a step. The step's OperationId is
        // deterministic across replays, so the key stays the same on every attempt.
        string idempotencyKey = await ctx.StepAsync(
            async (stepCtx, _) => stepCtx.OperationId,
            name: "idempotency-key");

        return await ctx.StepAsync(
            async (_, ct) => await PaymentService.ChargeAsync(
                input.Amount, input.CardToken, idempotencyKey, ct),
            name: "charge");
    }
}

public record PaymentEvent(decimal Amount, string CardToken);
public record Receipt(string TransactionId);

public static class PaymentService
{
    public static Task<Receipt> ChargeAsync(
        decimal amount, string cardToken, string idempotencyKey, CancellationToken ct)
        => Task.FromResult(new Receipt(idempotencyKey));
}
