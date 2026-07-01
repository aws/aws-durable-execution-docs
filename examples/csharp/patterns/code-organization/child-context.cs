using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ChildContextExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<Order, string>(Workflow, input, context);

    private async Task<string> Workflow(Order order, IDurableContext ctx)
    {
        // Group the order-processing operations into one child context.
        return await ctx.RunInChildContextAsync(
            async (child, ct) =>
            {
                await child.StepAsync(
                    async (_, _) => Validate(order),
                    name: "validate");
                Receipt receipt = await child.StepAsync(
                    async (_, _) => Charge(order),
                    name: "charge");
                await child.StepAsync(
                    async (_, _) => Schedule(order, receipt),
                    name: "schedule");
                return "ok";
            },
            name: "process-order");
    }

    private static ValidationResult Validate(Order order) => new(order.Id, Valid: true);
    private static Receipt Charge(Order order) => new(order.Id, order.Total);
    private static string Schedule(Order order, Receipt receipt) => $"ship-{order.Id}";
}

public record Order(string Id, decimal Total);
public record ValidationResult(string OrderId, bool Valid);
public record Receipt(string OrderId, decimal Amount);
