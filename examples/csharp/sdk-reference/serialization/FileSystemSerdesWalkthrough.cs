using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

public class FileSystemSerdesWalkthrough
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Report>(Workflow, input, context);

    private async Task<Report> Workflow(object input, IDurableContext ctx)
    {
        // FileSystemSerializer wraps an inner ILambdaSerializer (here the default JSON
        // serializer) and writes each result to the mounted filesystem, keeping only a small
        // file pointer in the checkpoint. Pass it to a single operation's config; other
        // operations continue to use the serializer registered at the host boundary.
        var fileSystem = new FileSystemSerializer(
            inner: new DefaultLambdaJsonSerializer(),
            basePath: "/mnt/efs");

        Report report = await ctx.StepAsync(
            async (_, _) => await BuildLargeReportAsync(),
            name: "build-report",
            config: new StepConfig { Serializer = fileSystem });

        return report;
    }

    private static Task<Report> BuildLargeReportAsync() => Task.FromResult(new Report("…large body…"));
}

public record Report(string Body);
