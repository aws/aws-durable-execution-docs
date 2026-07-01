using System.Linq;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class ChildContextTest
{
    private static async Task<string> Workflow(object input, IDurableContext ctx)
    {
        return await ctx.RunInChildContextAsync(
            async (child, _) =>
            {
                string a = await child.StepAsync(async (_, _) => "result-a", name: "step-a");
                string b = await child.StepAsync(async (_, _) => "result-b", name: "step-b");
                return $"{a}:{b}";
            },
            name: "process");
    }

    [Fact]
    public async Task ExecutesStepsInsideChildContext()
    {
        await using var runner = new DurableTestRunner<object, string>(Workflow);

        TestResult<string> result = await runner.RunAsync(new object());

        Assert.True(result.IsSucceeded);
        Assert.Equal("result-a:result-b", result.Result);

        var contextOps = result.Steps.Where(s => s.Kind == OperationKind.Context).ToList();
        Assert.NotEmpty(contextOps);
        Assert.Equal("process", contextOps[0].Name);
    }
}
