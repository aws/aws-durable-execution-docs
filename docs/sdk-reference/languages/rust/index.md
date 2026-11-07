# Rust SDK

!!! note "Preview"

    This is an experimental preview not intended for production use. The API may change
    without notice, and the final version may look different from what you see today.

The SDK crate (`aws-durable-execution-sdk`, library `aws_durable_execution_sdk`) runs in
your Lambda function. It provides `DurableContext`, the durable operations as methods
that return builders, logging through `tracing` with replayed records suppressed,
per-operation serialization, plugins, and local and cloud test runners. The examples
import the crate as `durable`, with `use aws_durable_execution_sdk as durable;`.

The SDK requires Rust 1.94.1 or later.

## Installation

Add the SDK from its GitHub repository, with the crates a durable function uses:

```console
cargo add aws-durable-execution-sdk --git https://github.com/aws/aws-durable-execution-sdk-rust
cargo add lambda_runtime serde_json tracing
cargo add serde --features derive
cargo add tokio --features macros
```

The test runners are in the `test_util` module, behind the `test-util` feature. Enable
the feature for tests with a dev-dependency on the same source:

```console
cargo add --dev aws-durable-execution-sdk --git https://github.com/aws/aws-durable-execution-sdk-rust --features test-util
```

To read the API documentation, run `cargo doc --open` in a crate that depends on the
SDK. See [Development environment](../../../getting-started/development-environment.md)
for the local toolchain and the deployment tools.

## The handler

A durable handler is an async function of an event and a `durable::DurableContext` that
returns `Result<O, durable::BoxError>`. `durable::run` registers it with the Lambda
runtime and runs one execution per invocation. You define the event and output types.
The event type needs `Deserialize` and the output type needs `Serialize`, and the SDK
decodes and encodes them with `serde_json`. `BoxError` is a boxed `std::error::Error`,
so `?` converts any error type.

```rust
--8<-- "examples/rust/sdk-reference/languages/handler-signature.rs"
```

The whole function is one `src/main.rs` with a `#[tokio::main] async fn main` that
calls `durable::run`. `durable::run_with_options` also takes a `durable::Options`
value, for example with a
[custom Lambda client](../../configuration/custom-lambda-client.md). `durable::wrap`
returns the service function without starting the runtime, for a program that adds its
own middleware and passes the result to `lambda_runtime::service_fn`. The
[Quickstart](../../../getting-started/quickstart.md) builds and deploys a complete
handler.

```rust
--8<-- "examples/rust/sdk-reference/languages/handler.rs"
```

When the handler returns an error, the execution settles `FAILED`. The SDK reports the
failure inside a successful Lambda response, so the Lambda `Errors` metric, dead-letter
queues, `OnFailure` destinations, and X-Ray errors do not fire. Monitor the durable
execution status instead. An error your code creates records the type `Error`, unless a
`durable::TypedError` in its chain names another type. A serialized result over
6,291,406 bytes is checkpointed as the execution result, and the response carries an
empty result.

## Build and deploy

Build the function with [cargo-lambda](https://www.cargo-lambda.info/), a Cargo
subcommand that builds Lambda functions:

```console
cargo lambda build --release --x86-64 --output-format zip
```

The `provided.al2023` runtime ships glibc 2.34, so a binary that a plain `cargo build`
links against a newer glibc fails at startup. cargo-lambda links through Zig, which pins
the glibc version. The build writes
`target/lambda/<package>/bootstrap.zip`, which holds the `bootstrap` binary that the
runtime runs. Without `--output-format zip`, it writes the binary itself,
`target/lambda/<package>/bootstrap`. Use `--arm64` with `--architectures arm64` for
Graviton. Deploy on `provided.al2023` with `--handler bootstrap`. The
[Quickstart](../../../getting-started/quickstart.md) shows the full
`aws lambda create-function` command. To build with the SAM CLI, set
`BuildMethod: rust-cargolambda` in the function's `Metadata`, as
[Development Environment](../../../getting-started/development-environment.md#4-deploy)
shows.

## The context

`durable::DurableContext` is the handle to the execution. It is cheap to clone, because
it is backed by an `Arc`, and it is `Send` and `Sync`. Besides the durable operations,
it exposes `execution_arn()`, `lambda_context()`, which returns the Lambda context with
the request ID and the deadline, and `is_replaying()`.

A step body receives a `durable::StepContext` instead. Its `attempt()` method returns
the attempt number, starting at 1. A step is a single atomic unit of work, so an
operation called inside a step body through a `DurableContext` clone fails the
execution with a context usage error. See
[Key concepts](../../../getting-started/key-concepts.md#durablecontext) for the execution
model and [Step](../../operations/step.md) for the step body.

## Builders, futures, and `.spawn()`

Every operation method returns a builder, and awaiting the builder runs the operation.
Options are builder methods, such as `.name("...")`, which names the operation in the
execution history. `.spawn()` starts the operation at once and returns a
`durable::DurableFuture<O>`, so you can overlap independent work and await it later,
for example with `tokio::join!`. Most builders also have `.future()`, which converts the
builder into a `durable::DurableFuture<O>` without starting it, for the combinators
below. Each operation reference, such as [Step](../../operations/step.md) and
[Wait](../../operations/wait.md), lists its builder's options and what `.spawn()` and
`.future()` return.

`ctx.invoke::<O>(..)` and `ctx.wait_for_callback::<O>(..)` take the output type `O` as
their only type parameter. A `wait_for_condition` builder needs a wait strategy from
`.wait_strategy(..)` or `.wait_strategy_fn(..)`. Without one, code that awaits the
builder or calls `.future()` or `.spawn()` on it does not compile.

```rust
--8<-- "examples/rust/sdk-reference/languages/concurrency.rs"
```

Dropping the handle that `.spawn()` returns discards the result. The operation keeps
running until it settles, until the scope it was started in closes, or until the
invocation ends. Returning from the handler ends the invocation and stops each detached
operation that still runs, so await the handle of every operation that must finish. A
stopped operation fires no
[plugin end hook](../../observability/plugins.md#operation-hooks), and its record can
stay `STARTED` while the execution finishes with the handler's result.

## Concurrency and determinism

The SDK assigns each operation its ID when you call the operation method, in the order
the handler calls them, and replay hands each recorded result to the operation with the
same ID. So replay is correct when the handler creates the same operations in the same
order on every invocation. Operations that you start with `.spawn()` and join with
`tokio::join!` get the same IDs on every run, as long as the handler calls their
operation methods in the same order. A combinator claims its ID later, when it is first
awaited, polled, or spawned. A default `with_retry` block claims its ID when it is
awaited or spawned, or when `.future()` converts it. Await each of them where the
handler awaited it on the first run.

These rules keep the order the same:

- Decide which operations to create only from the event and from results the SDK
    already recorded.
- Do not create operations while iterating a `HashMap` or a `HashSet`, whose iteration
    order changes between runs. Sort the entries into a `Vec` first, or use a `BTreeMap`.
- Race durable futures with `ctx.race` or `ctx.select_ok`, which record which branch
    won. Do not use `tokio::select!` over durable futures.
- Start an operation in the background with `.spawn()` on its builder. Do not use
    `tokio::spawn`. An operation claimed, spawned, or awaited on a task or thread that
    does not own the context fails the execution with a context usage error.
- Read the clock or generate random values inside a step body. Only the step's recorded
    result takes part in replay.

Replay compares each operation's type, subtype, and name with the record. A mismatch
fails the execution with a `durable::OperationError` whose kind is
`durable::OperationErrorKind::NonDeterministicExecution`. See
[Determinism](../../../patterns/best-practices/determinism.md) for the replay rules and
[Child context](../../operations/child-context.md#concurrency) for running operations
concurrently.

The invocation suspends when no SDK work can make progress and an operation waits on
the service. The values the handler holds are dropped at that moment, so a guard that
releases a resource in `Drop` releases it when the invocation suspends, before the
operation completes. Release resources in a step instead. Await other async work, such
as an HTTP call, inside a step, because awaiting it between operations can let the
invocation suspend there.

## Combinators and batches

`ctx.try_join_all`, `ctx.join_all`, `ctx.select_ok`, and `ctx.race` take
`durable::DurableFuture`s from `.future()` or `.spawn()`. Each records its outcome as
one operation, so replay returns the same outcome. `try_join_all` collects every result
and fails on the first error. `join_all` collects every outcome as a
`Result<O, durable::OperationError>`. `select_ok` returns the first success, and `race`
returns the first outcome to settle.

```rust
--8<-- "examples/rust/sdk-reference/languages/combinators.rs"
```

Awaiting `ctx.map` or `ctx.parallel` returns the succeeded values in input order. It
returns a `durable::OperationError` when the failure tolerance is exceeded, which the
default fail-fast configuration does at the first failed item or branch. Pass a
`durable::builders::map_parallel::CompletionConfig` to `.completion(..)` to set another
tolerance. Its builder's `build()` returns a `CompletionConfigValidationError` for a
tolerated failure percentage that is not finite or lies outside 0 to 100. Items and
branches still running are then cancelled, and those not yet started never run.
`.await_batch()` returns a `durable::builders::map_parallel::BatchResult` with each
started item's result or error and the completion reason.
`durable::builders::map_parallel::BatchResult::into_result()` propagates the first
failed item. For branches with different output types, build a
`durable::BranchSet`, run `ctx.parallel_mixed`, and read each result from its
`durable::BranchHandle`. The `parallel_mixed` builder starts eagerly with `.spawn()`.
Its `.summary(...)` sets a summary for a batch result too large to checkpoint. The
summary reads each branch's value with
`durable::builders::map_parallel::BatchResult::value` and the `durable::BranchKey` that
`BranchHandle::key` returns.

See [Map](../../operations/map.md) and [Parallel](../../operations/parallel.md) for the
operations that fan out work.

## Errors and panics

Every operation returns `Result<T, durable::OperationError>`. Match `err.kind()`
against `durable::OperationErrorKind`. Each operation variant holds a specific error
whose `kind()` gives details such as `durable::StepErrorKind::Interrupted`,
`durable::WaitErrorKind::UnexpectedStatus`,
`durable::CallbackErrorKind::HeartbeatTimedOut`, or
`durable::WaitForConditionErrorKind::MaxChecksExceeded`. Read the operation ID and wire
status with `operation_id()` and `status()`. `wire()` returns the recorded type,
message, data, and stack trace when a wire record exists. It gives the same record on
the live and replay paths. Every kind enum is `#[non_exhaustive]`, so a `match` needs a
`_` arm. Print the whole error chain with `{err:#}`.

```rust
--8<-- "examples/rust/sdk-reference/languages/errors.rs"
```

Return an `Err` to fail an operation. A panic fails the Lambda invocation instead. A
panic in the handler, a step body, a condition check, a child body, a map item, a
parallel branch, or a combinator input propagates unchanged. The SDK records nothing
for it, and the service invokes the function again. A panic that happens on every
attempt therefore repeats until the execution times out. With `panic = "abort"`, the
process exits at the panic and cleanup does not run. See
[Error handling](../../error-handling/errors.md) for the error types.

## Retries

Build a retry strategy with `durable::builders::RetryStrategyConfig`, or pass a closure
to `.retry_strategy(..)` that receives the `durable::StepError` and 1-based failed
attempt. A step without a strategy uses `RetryStrategyConfig::default_preset()`, which
makes 6 attempts. A retry delay suspends the invocation only when all remaining SDK work
is blocked. A retry that becomes due while other work runs starts in the same
invocation.

`ctx.with_retry` retries a block of durable operations as one unit. By default it
records an outer child context and one child context per attempt.
`.without_child_context()` records attempts and waits in the caller's context.
`.virtual_context()` keeps a separate operation namespace without an outer record. See
[Retries](../../error-handling/retries.md) for retry strategies, delay rounding, and
submitter retries for `wait_for_callback`.

## Logging

Log with the macros of the [`tracing`](https://docs.rs/tracing) crate.
`durable::init_default_subscriber()` installs a subscriber with the Lambda runtime's
default settings and `durable::ReplayFilterLayer`, so a record between operations is
written once per execution. A valid `RUST_LOG` directive decides which records it
writes. When `RUST_LOG` is unset or holds no valid directive, the level comes from
`AWS_LAMBDA_LOG_LEVEL`, else `INFO`. The subscriber writes JSON when
`AWS_LAMBDA_LOG_FORMAT` is `json`. Lambda gives a durable function JSON log format by
default, so the default subscriber writes JSON there.

Each JSON record is one object on one line, with every field at the top level. The keys
are `requestId`, `timestamp`, `level`, `executionArn`, `tenantId`, `operationId`,
`operationName`, and `attempt`, then the fields of the record and of its spans, then
`message`. `timestamp` is the UTC time to the millisecond. A correlation key without a
value is left out. A span that a plugin enters around `next()` in `wrap_invocation` or
`wrap_user_function` adds its fields, such as a trace ID, to the records emitted inside
it.

To build your own subscriber, add `ReplayFilterLayer` to its output layer. The SDK
never installs a subscriber itself. See [Logging](../../observability/logging.md).

## Configuration

`durable::Options::builder()` builds the `Options` that `run_with_options` takes:

- `lambda_client` and `sdk_config` supply the Lambda client or the configuration the SDK
    builds it from. See [Custom Lambda client](../../configuration/custom-lambda-client.md).
- `execution_client` replaces the transport of the two service calls, checkpoint and
    state read, with your own `durable::ExecutionClient`.
- `checkpoint_batching` sends checkpoints that arrive while a write is in flight
    together in the next request. It is on by default. `checkpoint_batching(false)`
    sends each checkpoint in its own request.
- `checkpoint_delay` holds checkpoint writes for up to the given duration, so that
    checkpoints from concurrent operations share a request.
- `plugin` registers a plugin, and `plugin_child_operations_depth` keeps the operations
    of finished child contexts visible to plugins in later invocations.

The default Lambda client has a 5 second connect timeout, a 50 second read timeout, and
a 55 second attempt timeout. The SDK adds
`aws-durable-execution-sdk-rust/<version>` to the `User-Agent` of requests sent through
`durable::LambdaExecutionClient`. The crate root re-exports `LambdaClient`, `SdkConfig`,
`LambdaContext`, `lambda_runtime`, and `lambda_types`.

A custom `durable::ExecutionClient` returns `durable::ExecutionClientError`.
`durable::ExecutionClientErrorScope::Invocation` ends the current invocation so replay
can resume it. `durable::ExecutionClientErrorScope::Execution` fails the durable
execution. `durable::LambdaExecutionClient` is the default transport and can be wrapped
by another implementation. `build()` returns a `durable::OptionsValidationError` for
settings that conflict, such as `execution_client` together with `lambda_client`.

## Plugins

A plugin implements `durable::plugin::Plugin`. A `durable::plugin::PluginFactory`
returns zero or one plugin for each invocation, and `durable::OptionsBuilder::plugin`
registers the factory. `durable::OptionsBuilder::build` rejects two factories whose
`exclusive_group()` values are the same nonempty string. Hooks observe invocations,
operations, attempts, checkpoint changes, and user functions. A synchronous hook can
return asynchronous `durable::plugin::HookWork`. See
[Plugins](../../observability/plugins.md) for the hook order and the wrap hooks.

## Serialization

Operation values use a `durable::Serdes<T>` selected on each builder. Step, child
context, `with_retry`, map and parallel item results, and `wait_for_condition` state
round-trip through `serialize` and `deserialize`. Invoke results and callback payloads
come from outside the SDK and use only `deserialize`. The default is
`durable::JsonSerdes`. A map or parallel whole-result serdes processes the batch record
as JSON text and defaults to `durable::PassthroughSerdes`.

Share one serdes across operations with `std::sync::Arc<S>`. Serdes errors are permanent
unless a `durable::serdes::RetryableSerdesError` marks them transient.
`durable::serdes::FileSystemSerdes` stores values on a durable mounted filesystem and
keeps a file reference in the checkpoint. See
[Serialization](../../state/serialization.md).

## Testing

`durable::test_util::LocalRunner` runs a handler in process through the production
replay loop. It uses a virtual clock, so waits and retry delays end without real
waiting. Awaiting `runner.run(handler, event)` drives the execution until it reaches a
terminal state or nothing can advance it. Queue callback replies, registered invoke
targets, and checkpoint failures on the runner before the run. Use a
`durable::test_util::RunHandle` and its operation handles to inspect, pause, resume, or
answer a run in progress. After the run, `durable::test_util::TestResult` exposes the
output, operations, history events, children, and invocation count.

`durable::test_util::CloudRunner` invokes a deployed function and folds its durable
execution history into the same `durable::test_util::TestResult` form. Configure its qualifier, Region,
poll interval, poll budget, and invocation type. A spent poll budget returns a
result with `durable::test_util::ExecutionStatus::Running`. See
[Testing](../../../testing/index.md) for the runners and the assertions.

## Source and reference

The source and the SDK README are in
[aws/aws-durable-execution-sdk-rust](https://github.com/aws/aws-durable-execution-sdk-rust).
