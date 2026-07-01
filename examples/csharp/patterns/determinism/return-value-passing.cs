using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ReturnValuePassingExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderBatch, BatchTotal>(Workflow, input, context);

    // Right: each step returns the new running total, restored from checkpoint on replay.
    private async Task<BatchTotal> Workflow(OrderBatch input, IDurableContext ctx)
    {
        decimal total = 0m;
        foreach (Item item in input.Items)
        {
            decimal running = total;
            total = await ctx.StepAsync(
                async (_, _) =>
                {
                    SaveItem(item);
                    return running + item.Price;
                },
                name: $"save-{item.Id}");
        }
        return new BatchTotal(total);
    }

    // Wrong: total mutates outside the step, replay restarts it at 0.
    private async Task<BatchTotal> BrokenWorkflow(OrderBatch input, IDurableContext ctx)
    {
        decimal total = 0m;
        foreach (Item item in input.Items)
        {
            await ctx.StepAsync(async (_, _) => SaveItem(item), name: $"save-{item.Id}");
            total += item.Price; // Lost on replay!
        }
        return new BatchTotal(total);
    }

    private static void SaveItem(Item item)
    {
        // persist item
    }
}

public record Item(string Id, decimal Price);
public record OrderBatch(IReadOnlyList<Item> Items);
public record BatchTotal(decimal Total);
