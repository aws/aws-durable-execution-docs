using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class StepNamesExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, string>(Workflow, input, context);

    private async Task<string> Workflow(OrderEvent input, IDurableContext ctx)
    {
        // Stable, descriptive name.
        ValidationResult validation = await ctx.StepAsync(
            async (_, _) => ValidateOrder(input),
            name: "validate-order");

        // Dynamic but deterministic: include the item ID from the input.
        await ctx.StepAsync(
            async (_, _) => SaveItem(input.Item),
            name: $"save-item-{input.Item.Id}");

        return validation.OrderId;
    }

    private static ValidationResult ValidateOrder(OrderEvent order) => new(order.Item.Id, Valid: true);
    private static void SaveItem(Item item) { /* persist item */ }
}

public record OrderEvent(Item Item);
public record Item(string Id);
public record ValidationResult(string OrderId, bool Valid);
