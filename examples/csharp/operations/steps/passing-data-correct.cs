using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class PassingDataCorrectExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<UserEvent>(Workflow, input, context);

    // CORRECT: userId is returned from the step and restored from checkpoint on replay
    private async Task Workflow(UserEvent input, IDurableContext ctx)
    {
        string userId = await ctx.StepAsync(
            async (_, _) => RegisterUser(input.Email),
            name: "register-user");

        await ctx.WaitAsync(TimeSpan.FromMinutes(10), name: "follow-up-delay");

        await ctx.StepAsync(
            async (_, _) => SendFollowUpEmail(userId), // userId restored from checkpoint
            name: "send-follow-up-email");
    }

    private static string RegisterUser(string email) => $"user-{email}";

    private static void SendFollowUpEmail(string userId)
    {
        // send email to user
    }
}

public record UserEvent(string Email);
