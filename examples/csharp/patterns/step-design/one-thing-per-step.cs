using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class OneThingPerStepExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<Order, Receipt>(Workflow, input, context);

    private async Task<Receipt> Workflow(Order order, IDurableContext ctx)
    {
        // Right: each side effect gets its own step.
        Receipt receipt = await ctx.StepAsync(
            async (_, ct) => await ChargePayment(order, ct),
            name: "charge-payment");
        await ctx.StepAsync(
            async (_, ct) => await SendConfirmationEmail(order, ct),
            name: "send-confirmation");
        await ctx.StepAsync(
            async (_, ct) => await UpdateInventory(order, ct),
            name: "update-inventory");
        return receipt;
    }

    private static Task<Receipt> ChargePayment(Order order, CancellationToken ct)
        => Task.FromResult(new Receipt(order.OrderId, Charged: true));
    private static Task SendConfirmationEmail(Order order, CancellationToken ct) => Task.CompletedTask;
    private static Task UpdateInventory(Order order, CancellationToken ct) => Task.CompletedTask;
}

public record Order(string OrderId);
public record Receipt(string OrderId, bool Charged);
