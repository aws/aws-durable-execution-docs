using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class WaitForConditionExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<JobEvent, JobState>(Workflow, input, context);

    private async Task<JobState> Workflow(JobEvent input, IDurableContext ctx)
    {
        // Use an exponential-backoff wait strategy so an unresponsive downstream
        // system does not create a retry storm. The isDone predicate stops
        // polling once the state satisfies the condition.
        var strategy = WaitStrategy.Exponential<JobState>(
            maxAttempts: 60,
            initialDelay: TimeSpan.FromSeconds(5),
            maxDelay: TimeSpan.FromMinutes(1),
            backoffRate: 2.0,
            jitter: JitterStrategy.Full,
            isDone: state => state.Status == "completed");

        var config = new WaitForConditionConfig<JobState>
        {
            InitialState = new JobState(input.JobId, "pending"),
            WaitStrategy = strategy,
        };

        // The SDK runs the check on each poll, applies the wait strategy between
        // polls (suspending the execution — no compute charge), and resumes when
        // the strategy stops. Each poll is a step.
        JobState finalState = await ctx.WaitForConditionAsync<JobState>(
            async (state, _, ct) =>
            {
                string status = await JobService.GetStatusAsync(state.JobId, ct);
                return state with { Status = status };
            },
            config,
            name: "wait-for-job");

        return finalState;
    }
}

public record JobEvent(string JobId);
public record JobState(string JobId, string Status);

public static class JobService
{
    public static Task<string> GetStatusAsync(string jobId, CancellationToken ct)
        => Task.FromResult("completed");
}
