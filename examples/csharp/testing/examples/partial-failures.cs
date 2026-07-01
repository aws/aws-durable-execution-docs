using System.Linq;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class PartialFailuresTest
{
    private static async Task<string> Workflow(object input, IDurableContext ctx)
    {
        await ctx.StepAsync(async (_, _) => "ok", name: "step-1");
        await ctx.StepAsync(async (_, _) => "ok", name: "step-2");
        return await ctx.StepAsync<string>(
            async (_, _) => throw new InvalidOperationException("step-3 failed"),
            name: "step-3");
    }

    [Fact]
    public async Task RecordsWhichStepsSucceededBeforeFailure()
    {
        await using var runner = new DurableTestRunner<object, string>(Workflow);

        TestResult<string> result = await runner.RunAsync(new object());

        Assert.True(result.IsFailed);

        Assert.Equal(OperationStatus.Succeeded, result.GetStep("step-1").Status);
        Assert.Equal(OperationStatus.Succeeded, result.GetStep("step-2").Status);
        Assert.NotNull(result.Error);
    }
}
