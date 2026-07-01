using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class StableBranchesExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object>(Workflow, input, context);

    // Right: the SDK checkpoints the decision, so replay walks the same branch.
    private async Task Workflow(object input, IDurableContext ctx)
    {
        string shift = await ctx.StepAsync(
            async (_, _) => DateTime.UtcNow.Hour < 12 ? "morning" : "afternoon",
            name: "pick-shift");

        if (shift == "morning")
        {
            await ctx.StepAsync(async (_, _) => RunMorning(), name: "morning-work");
        }
        else
        {
            await ctx.StepAsync(async (_, _) => RunAfternoon(), name: "afternoon-work");
        }
    }

    // Wrong: replay may see a different value of DateTime.UtcNow.
    private async Task BrokenWorkflow(object input, IDurableContext ctx)
    {
        if (DateTime.UtcNow.Hour < 12)
        {
            await ctx.StepAsync(async (_, _) => RunMorning(), name: "morning-work");
        }
        else
        {
            await ctx.StepAsync(async (_, _) => RunAfternoon(), name: "afternoon-work");
        }
    }

    private static void RunMorning()
    {
        // morning workload
    }

    private static void RunAfternoon()
    {
        // afternoon workload
    }
}
