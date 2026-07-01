using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class HeartbeatTimeoutExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<JobEvent, JobResult>(Workflow, input, context);

    private async Task<JobResult> Workflow(JobEvent input, IDurableContext ctx)
    {
        // HeartbeatTimeout fails the callback if the external worker stops
        // checking in, even before the overall Timeout elapses. Set it
        // comfortably longer than the expected interval between heartbeats but
        // shorter than the overall operation timeout.
        var config = new WaitForCallbackConfig
        {
            Timeout = TimeSpan.FromHours(24),
            HeartbeatTimeout = TimeSpan.FromMinutes(10),
        };

        // The external system must call the SDK's heartbeat endpoint periodically
        // while the work is in progress, and the success/failure endpoint when done.
        JobResult outcome = await ctx.WaitForCallbackAsync<JobResult>(
            async (callbackId, _, ct) =>
                await JobService.StartAsync(input.JobId, callbackId, ct),
            name: "long-running-job",
            config: config);

        return outcome;
    }
}

public record JobEvent(string JobId);
public record JobResult(string Status);

public static class JobService
{
    public static Task StartAsync(string jobId, string callbackId, CancellationToken ct)
        => Task.CompletedTask;
}
