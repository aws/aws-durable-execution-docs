using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class NonDeterministicInStepExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<ChargeInput, ChargeResult>(Workflow, input, context);

    private async Task<ChargeResult> Workflow(ChargeInput input, IDurableContext ctx)
    {
        // Wrap the non-deterministic call in a step so the value is checkpointed.
        string transactionId = await ctx.StepAsync(
            async (_, _) => Guid.NewGuid().ToString(),
            name: "generate-transaction-id");

        Receipt receipt = await ctx.StepAsync(
            async (_, _) => Charge(input.Amount, transactionId),
            name: "charge");

        return new ChargeResult(transactionId, receipt);
    }

    private static Receipt Charge(decimal amount, string transactionId)
        => new(transactionId, amount);
}

public record ChargeInput(decimal Amount);
public record Receipt(string TransactionId, decimal Amount);
public record ChargeResult(string TransactionId, Receipt Receipt);
