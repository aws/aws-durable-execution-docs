using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class PassingDataWrongExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<UserEvent>(Workflow, input, context);

    // WRONG: userId mutation is lost on replay after the wait
    private async Task Workflow(UserEvent input, IDurableContext ctx)
    {
        string userId = "";

        await ctx.StepAsync(
            async (_, _) => { userId = RegisterUser(input.Email); }, // Lost on replay!
            name: "register-user");

        await ctx.WaitAsync(TimeSpan.FromMinutes(10), name: "follow-up-delay");

        await ctx.StepAsync(
            async (_, _) => SendFollowUpEmail(userId), // userId is "" on replay
            name: "send-follow-up-email");
    }

    private static string RegisterUser(string email) => $"user-{email}";

    private static void SendFollowUpEmail(string userId)
    {
        // send email to user
    }
}

public record UserEvent(string Email);
