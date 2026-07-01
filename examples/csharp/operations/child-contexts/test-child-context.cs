using System.Linq;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class TestChildContext
{
    [Fact]
    public async Task ChildContextSucceeds()
    {
        await using var runner = new DurableTestRunner<OrderEvent, ChargedOrder>(
            new BasicChildContextExample().Workflow);

        TestResult<ChargedOrder> result = await runner.RunAsync(new OrderEvent("order-1"));

        Assert.True(result.IsSucceeded);
        Assert.Equal("order-1", result.Result!.OrderId);
        Assert.True(result.Result.Charged);
    }

    [Fact]
    public async Task RecordsContextOperation()
    {
        await using var runner = new DurableTestRunner<OrderEvent, ChargedOrder>(
            new BasicChildContextExample().Workflow);

        TestResult<ChargedOrder> result = await runner.RunAsync(new OrderEvent("order-1"));

        // The testing SDK records child context operations as CONTEXT-kind steps.
        var contextOps = result.Steps.Where(s => s.Kind == OperationKind.Context).ToList();
        Assert.NotEmpty(contextOps);
        Assert.Equal(OperationStatus.Succeeded, contextOps[0].Status);
    }
}
