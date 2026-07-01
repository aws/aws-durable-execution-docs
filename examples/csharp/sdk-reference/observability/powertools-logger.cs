using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

public class PowertoolsLoggerExample
{
    // The Powertools for AWS Lambda (.NET) logger is an ILogger, so it plugs in
    // as the CustomLogger. Replace this placeholder with the Powertools logger,
    // for example the ILogger produced by services.AddPowertoolsLogging(...).
    private static readonly ILogger PowertoolsLogger = NullLogger.Instance;

    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        ctx.ConfigureLogger(new LoggerConfig { CustomLogger = PowertoolsLogger });
        ctx.Logger.LogInformation("Running handler");

        return await Task.FromResult("ok");
    }
}
