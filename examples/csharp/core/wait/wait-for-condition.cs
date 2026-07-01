using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class WaitForConditionExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, JobState>(Workflow, input, context);

    private async Task<JobState> Workflow(object input, IDurableContext ctx)
    {
        // Poll until the job completes.
        var result = await ctx.WaitForConditionAsync(
            check: async (state, checkCtx, ct) =>
            {
                var status = await GetJobStatus(state.JobId, ct);
                return state with { Status = status, Done = status == "COMPLETED" };
            },
            config: new WaitForConditionConfig<JobState>
            {
                InitialState = new JobState("job-123", "pending", Done: false),
                WaitStrategy = WaitStrategy.Exponential<JobState>(
                    maxAttempts: 60,
                    initialDelay: TimeSpan.FromSeconds(5),
                    maxDelay: TimeSpan.FromMinutes(5),
                    backoffRate: 2.0,
                    jitter: JitterStrategy.None,
                    isDone: state => state.Done),
            },
            name: "wait_for_job");
        return result;
    }

    private static Task<string> GetJobStatus(string jobId, CancellationToken ct)
        => Task.FromResult("COMPLETED");
}

public record JobState(string JobId, string Status, bool Done);
