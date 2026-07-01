using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class PollingTest
{
    public record PollState(int Attempts, bool Done);

    private static async Task<PollState> Workflow(object input, IDurableContext ctx)
    {
        return await ctx.WaitForConditionAsync(
            async (state, _, _) => new PollState(state.Attempts + 1, state.Attempts >= 2),
            new WaitForConditionConfig<PollState>
            {
                InitialState = new PollState(0, false),
                WaitStrategy = WaitStrategy.Fixed<PollState>(
                    TimeSpan.FromSeconds(1),
                    isDone: state => state.Done),
            },
            name: "poll-job");
    }

    [Fact]
    public async Task PollsUntilConditionIsMet()
    {
        await using var runner = new DurableTestRunner<object, PollState>(Workflow);

        TestResult<PollState> result = await runner.RunAsync(new object());

        Assert.True(result.IsSucceeded);
        Assert.True(result.Result!.Done);
    }
}
