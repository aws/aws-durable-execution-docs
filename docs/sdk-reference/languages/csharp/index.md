# C# SDK

The .NET SDK (`Amazon.Lambda.DurableExecution`) runs in your Lambda functions and
provides `IDurableContext`, the durable operations, replay-aware logging, and pluggable
serialization. The durable operations and execution model match the other SDKs. What
differs is the host surface: your handler receives the service envelope, you call
`DurableFunction.WrapAsync` yourself, and every operation body takes a `CancellationToken`.

## Installation

Add the package to your Lambda function project:

```console
dotnet add package Amazon.Lambda.DurableExecution
```

## Usage

Your Lambda handler receives a `DurableExecutionInvocationInput` and returns a
`DurableExecutionInvocationOutput`. These are the service envelope: the durable
execution service sends the first, and reads the second to learn whether the workflow
succeeded, failed, or suspended. Delegate to `DurableFunction.WrapAsync<TInput, TOutput>`,
which unpacks your input from the envelope, runs your workflow with an `IDurableContext`,
and packs the result back into the envelope.

```csharp
using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.RuntimeSupport;
using Amazon.Lambda.Serialization.SystemTextJson;

namespace OrderProcessor;

public class OrderProcessor
{
    public static async Task Main()
    {
        var handler = new OrderProcessor();
        var serializer = new DefaultLambdaJsonSerializer();
        using var wrapper = HandlerWrapper.GetHandlerWrapper<DurableExecutionInvocationInput, DurableExecutionInvocationOutput>(
            handler.Handler, serializer);
        using var bootstrap = new LambdaBootstrap(wrapper);
        await bootstrap.RunAsync();
    }

    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<Order, OrderResult>(Workflow, input, context);

    private async Task<OrderResult> Workflow(Order order, IDurableContext ctx)
    {
        var reservation = await ctx.StepAsync(
            async (_, ct) => await InventoryService.ReserveAsync(order.Items, ct),
            name: "reserve-inventory");

        await ctx.WaitAsync(TimeSpan.FromHours(2), name: "warehouse-processing");

        var shipment = await ctx.StepAsync(
            async (_, ct) => await ShippingService.ShipAsync(reservation, order.Address, ct),
            name: "confirm-shipment");

        return new OrderResult(order.Id, shipment.TrackingNumber);
    }
}
```

`WrapAsync` has a void overload, `WrapAsync<TInput>(Func<TInput, IDurableContext, Task>, ...)`,
for workflows that return nothing, and overloads that accept an explicit `IAmazonLambda`
client when you need to customize the client used for `InvokeAsync` and checkpointing.

Operation names are optional. When you omit `name`, the SDK infers one from the call
site and derives the deterministic operation ID from it. Pass stable, descriptive names
so replay matches new execution to checkpointed state and so operations read clearly in
logs and traces.

## Programming models

The other SDKs offer one handler pattern each. .NET gives you two, plus a code generator.
All three run on the managed `dotnet10` runtime.

The **executable model** shown above owns its entry point. `Main` builds a
`LambdaBootstrap` with your handler and an `ILambdaSerializer`, then calls `RunAsync()`.

The **class-library model** skips the `Main` and `LambdaBootstrap` loop. The managed
runtime supplies the bootstrap and invokes your handler directly. Declare the serializer
with an assembly attribute and deploy with an `Assembly::Namespace.Type::Method` handler
string (for example, `OrderProcessor::OrderProcessor.OrderProcessor::Handler`):

```csharp
using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

[assembly: LambdaSerializer(typeof(DefaultLambdaJsonSerializer))]

namespace OrderProcessor;

public class OrderProcessor
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<Order, OrderResult>(Workflow, input, context);

    private async Task<OrderResult> Workflow(Order order, IDurableContext ctx)
    {
        // same workflow body as the executable model
    }
}
```

**Lambda Annotations** removes the handler and `WrapAsync` boilerplate. Add
[Amazon.Lambda.Annotations](https://github.com/aws/aws-lambda-dotnet/tree/master/Libraries/src/Amazon.Lambda.Annotations)
and annotate your workflow method with both `[LambdaFunction]` and `[DurableExecution]`.
The source generator emits the handler wrapper that calls `DurableFunction.WrapAsync`,
and writes the `DurableConfig` block and checkpoint-API IAM permissions into the
generated `serverless.template`.

```csharp
using Amazon.Lambda.Annotations;
using Amazon.Lambda.DurableExecution;

public class OrderProcessor
{
    [LambdaFunction]
    [DurableExecution(RetentionPeriodInDays = 7, ExecutionTimeout = 300)]
    public async Task<OrderResult> Workflow(Order order, IDurableContext ctx)
    {
        var reservation = await ctx.StepAsync(
            async (_, ct) => await InventoryService.ReserveAsync(order.Items, ct),
            name: "reserve-inventory");
        // remaining steps
        return new OrderResult(order.Id, reservation.TrackingNumber);
    }
}
```

Set `[assembly: LambdaGlobalProperties(GenerateMain = true)]` for the executable model, or
omit it for a class library. The generated `serverless.template` handler adapts to each.
The generator validates the `(TInput, IDurableContext) -> Task` or `Task<TOutput>`
signature and Zip packaging, and reports a diagnostic otherwise.

## Serialization

`WrapAsync` reads the `ILambdaSerializer` off `ILambdaContext.Serializer` and uses that
single serializer for every payload: your workflow input and output, and each step,
child context, callback, invoke, and condition-check result that gets checkpointed. You
register the serializer once, at the host boundary. In the executable model you pass it
to `HandlerWrapper.GetHandlerWrapper`; in the class-library model you declare it with
`[assembly: LambdaSerializer(...)]`.

For reflection-based serialization, use `DefaultLambdaJsonSerializer`. For trimmed or
Native AOT functions, register `SourceGeneratorLambdaJsonSerializer<TContext>` with your
`JsonSerializerContext` instead. AOT and reflection scenarios share one code path in the
SDK, so no operation takes a per-call serializer argument. The library ships as
`IsTrimmable`; keep your workflow and the types you checkpoint trim-clean so the AOT
analyzer stays quiet.

## Cancellation

Every user function that `IDurableContext` accepts, `StepAsync`,
`RunInChildContextAsync`, `WaitForCallbackAsync`, and `WaitForConditionAsync`, receives a
`CancellationToken`. The token is a linked source that combines the token you passed to
the operation with an SDK-owned workflow-shutdown signal. The shutdown signal fires when
the workflow is being torn down, for example when a sibling operation suspends or a
checkpoint fails.

Pass the token to cancellation-aware APIs inside the body so the body unwinds cleanly
when either trigger fires:

```csharp
var user = await ctx.StepAsync(
    async (_, ct) => await httpClient.GetAsync(url, ct),
    name: "fetch");
```

When the token fires and an `OperationCanceledException` propagates out of the body, the
SDK treats it as cancellation: it writes no failure checkpoint and consults no retry
strategy. An `OperationCanceledException` thrown for unrelated reasons, when the token
never fired, is a normal step failure that checkpoints and retries per the configured
`RetryStrategy`. The SDK's own writes, checkpoints and the runtime API response, never
observe the shutdown signal, so completed work is never lost to teardown.

Do not branch workflow logic on `IsCancellationRequested`. Cancellation is a runtime
concern, not a workflow concern, and branching on it makes replay non-deterministic. Do
not catch `OperationCanceledException` and continue. Either let it propagate, or catch
and rethrow.

## Logging

`IDurableContext.Logger` is a replay-safe `Microsoft.Extensions.Logging.ILogger`. It
suppresses messages emitted while the workflow re-derives prior operations from
checkpointed state, so a 30-step workflow re-invoked 30 times still emits each line once.
Use it instead of `Console.WriteLine`, which repeats on every replay. Call
`ConfigureLogger(LoggerConfig)` to swap the underlying logger (Serilog, AWS Lambda
Powertools) or set `ModeAware = false` to see every line on every replay while debugging.

## Source and reference

The source is in
[aws/aws-lambda-dotnet](https://github.com/aws/aws-lambda-dotnet/tree/master/Libraries/src/Amazon.Lambda.DurableExecution).
