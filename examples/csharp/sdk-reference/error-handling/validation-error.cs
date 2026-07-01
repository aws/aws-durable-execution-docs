using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class ValidationErrorExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, ValidationResult>(Workflow, input, context);

    private async Task<ValidationResult> Workflow(object input, IDurableContext ctx)
    {
        // The SDK throws ArgumentOutOfRangeException for invalid configuration
        // values. For example, passing a duration shorter than 1 second to
        // WaitAsync:
        try
        {
            await ctx.WaitAsync(TimeSpan.FromMilliseconds(500), name: "short-wait"); // invalid: less than 1 second
        }
        catch (ArgumentOutOfRangeException e)
        {
            ctx.Logger.LogError("Invalid SDK configuration: {Message}", e.Message);
            return new ValidationResult("InvalidConfiguration", e.Message);
        }
        return new ValidationResult("ok", null);
    }
}

public record ValidationResult(string Status, string? Message);
