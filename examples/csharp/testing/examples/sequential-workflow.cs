using System.Linq;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class SequentialWorkflowTest
{
    public record Order(string OrderId, string? Status = null, string? Payment = null, string? Fulfillment = null);

    private static async Task<Order> Workflow(Order input, IDurableContext ctx)
    {
        Order validated = await ctx.StepAsync(
            async (_, _) => input with { Status = "validated" },
            name: "validate");
        Order paid = await ctx.StepAsync(
            async (_, _) => validated with { Payment = "completed" },
            name: "payment");
        return await ctx.StepAsync(
            async (_, _) => paid with { Fulfillment = "shipped" },
            name: "fulfillment");
    }

    [Fact]
    public async Task ExecutesAllStepsInOrder()
    {
        await using var runner = new DurableTestRunner<Order, Order>(Workflow);

        TestResult<Order> result = await runner.RunAsync(new Order("order-123"));

        Assert.True(result.IsSucceeded);

        var stepOps = result.Steps.Where(s => s.Kind == OperationKind.Step).ToList();
        Assert.Equal(3, stepOps.Count);
        Assert.Equal(
            new[] { "validate", "payment", "fulfillment" },
            stepOps.Select(s => s.Name).ToArray());
    }
}
