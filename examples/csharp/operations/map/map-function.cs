using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class MapFunctionExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<IReadOnlyList<Order>, IReadOnlyList<Receipt>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<Receipt>> Workflow(
        IReadOnlyList<Order> orders, IDurableContext ctx)
    {
        IBatchResult<Receipt> result = await ctx.MapAsync(
            orders, ProcessOrder, name: "process-orders");
        return result.GetResults();
    }

    // The map function: receives (ctx, item, index, allItems, cancellationToken)
    private static async Task<Receipt> ProcessOrder(
        IDurableContext ctx, Order order, int index,
        IReadOnlyList<Order> orders, CancellationToken ct)
    {
        Order validated = await ctx.StepAsync(async (_, _) =>
        {
            if (order.Amount <= 0) throw new ArgumentException("Invalid amount");
            return order;
        }, name: "validate");

        decimal charged = await ctx.StepAsync(
            async (_, _) => validated.Amount, name: "charge");

        return new Receipt(validated.Id, charged);
    }
}

public record Order(string Id, decimal Amount);
public record Receipt(string OrderId, decimal Charged);
