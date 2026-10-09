# Go SDK

!!! note

    The Go SDK is an experimental preview, not intended for production use. The API may
    change without notice, and the final version may differ from what you see today.

The Go SDK (`github.com/aws/aws-durable-execution-sdk-go`) runs in your Lambda function.
It provides `durable.Context`, the durable operations as package-level generic
functions, replay-aware logging, and pluggable serialization. The operations are
functions you call with the context, such as `durable.Step(ctx, ...)`, and each context
is owned by the goroutine that created it.

The SDK requires Go 1.24 or later.

## Installation

Add the SDK to a module:

```console
go get github.com/aws/aws-durable-execution-sdk-go/durable
```

The preview has no release tags. `go get` records a pseudo-version of the latest commit
on `main`, such as `v0.0.0-20260920051622-7f3b62f70004`, and
`go get github.com/aws/aws-durable-execution-sdk-go/durable@latest` moves to a newer
commit. The local runner is package
`github.com/aws/aws-durable-execution-sdk-go/durable/durabletest`, in the same module.

## The handler

A durable handler is a function of a context and an event that returns a value and an
error. `durable.Start` registers it with the Lambda runtime and runs one execution per
invocation. You define the event and output types. The SDK decodes the event and encodes
the output with `encoding/json`.

```go
--8<-- "examples/go/sdk-reference/languages/handler-signature.go"
```

The whole function is one `package main` with a `func main` that calls `durable.Start`.
`durable.Wrap` returns a raw payload function instead of starting the runtime, for a
program that composes its own Lambda entry point. Do not pass `Wrap`'s result to
`lambda.Start` directly. `durable.Start` registers it through the runtime's raw byte
interface.

```go
--8<-- "examples/go/sdk-reference/languages/handler.go"
```

When the handler returns an error, the execution settles `FAILED`. The `ErrorType` of an
error from `errors.New` or `fmt.Errorf` is `Error`. The `ErrorType` of any other error
is the name of its Go type. An error whose chain holds an SDK error type reports that
type's name, so a `*durable.StepError` wrapped with `fmt.Errorf` reports `StepError`.

## Build and deploy

Build a static Linux binary named `bootstrap`, the file the `provided.al2023` runtime
runs:

```console
CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -tags lambda.norpc -o bootstrap .
```

`CGO_ENABLED=0` makes the binary static. `GOOS=linux GOARCH=amd64` cross-compiles for
Lambda on x86_64. Use `GOARCH=arm64` with `--architectures arm64` for Graviton. The
`-tags lambda.norpc` build tag drops the legacy RPC entry point from `aws-lambda-go`,
which the custom runtime does not use. The tag makes the binary smaller and is optional.
Deploy the zipped binary on `provided.al2023` with `--handler bootstrap`. The
[Quickstart](../../../getting-started/quickstart.md) shows the full
`aws lambda create-function` command.

## The context

`durable.Context` is the handle to the execution. It embeds `context.Context`, so you
can pass it to any function that takes a `context.Context`. Inside a step, pass the
step's `durable.StepContext` to AWS SDK calls. The context exposes `ExecutionArn()`,
`RequestID()`, `InvokedFunctionARN()`, `Logger()`, and `IsReplaying()`. Only the SDK can
implement it. Every operation takes the context as its first argument.

A step body, condition check, or callback submitter receives a `durable.StepContext`
instead. It also embeds `context.Context` and adds `Logger()` and `Attempt()`, but it
exposes no durable operations, because a step is a single atomic unit of work.

## Goroutines and `durable.Go`

A `durable.Context` is owned by the goroutine that created it. Calling an operation on
it from another goroutine fails with `durable.ErrWrongGoroutine` before the operation
claims an ID. Two goroutines claiming operations on one context would claim them in a
scheduling-dependent order, and replay would then pair recorded results with the wrong
operations.

Use `durable.Go` to run durable work concurrently. It claims the child operation on the
calling goroutine, which keeps the order deterministic, then runs the body on a new
goroutine with a fresh child context. Use the child context inside the body, never the
parent.

```go
--8<-- "examples/go/sdk-reference/languages/goroutine-signature.go"
```

```go
--8<-- "examples/go/sdk-reference/languages/goroutines.go"
```

The SDK enforces this at run time. An operation on an enclosing context from a step body
or a blocking `RunInChildContext` body fails with `durable.ErrWrongContext`. One from a
`Go` or `RunInChildContextAsync` body, a `Map` item, or a `Parallel` branch fails with
`durable.ErrWrongGoroutine`, because that goroutine does not own the enclosing context.
Either way the rejected call records nothing. Both checks run in every default build.
The `durablenocheck` build tag (`go build -tags durablenocheck`) compiles both checks
out. In that build the SDK never returns `ErrWrongGoroutine` or `ErrWrongContext`, so
such calls go undetected.

## Futures and combinators

`StepAsync`, `WaitAsync`, `InvokeAsync`, `RunInChildContextAsync`, and `Go` return a
`*durable.Future`. A `StepAsync`, `WaitAsync`, or `InvokeAsync` future that the handler
never awaits records no start operation. `RunInChildContextAsync` and `Go` record the
child context's start before they return, unless you pass `WithChildVirtual`. Await a
set of futures with a combinator, not with a sequence of `Result` calls. A sequence that
returns on the first error leaves the other futures unawaited. When the invocation then
suspends, their branches never reach a blocking point, so their progress is not
checkpointed. Once a combinator observes a suspension, it awaits all remaining futures
before it suspends. Before any suspension, `All` returns on the first failure, and `Any`
and `Race` return on the first winning outcome, without awaiting the remaining futures.
`Join` and `AllSettled` always await every future. Each combinator records its outcome
as one operation, so replay returns the same outcome.

```go
--8<-- "examples/go/sdk-reference/languages/combinator-signature.go"
```

`All`, `AllSettled`, `Any`, and `Race` take futures of one result type. `Join` takes
futures of different result types through the `Awaitable` interface, waits for all of
them, and returns the first error in argument order. Read the values afterwards with
`Result`. `Select` returns the name and value of the first branch to settle. See
[Parallel](../../operations/parallel.md) and [Map](../../operations/map.md) for the
operations that fan out work.

## Errors and the suspension signal

A non-nil error from an operation is one of three things. It can be a terminal failure
of that operation, which matches a public type such as `*durable.StepError` with
`errors.As`. It can be a plain error for an invalid argument or option, such as a `Wait`
duration under one second. Or it can be the signal that the invocation is suspending.
The last two match no public type. Return an error that matches no public type
unchanged. A typed failure carries the recorded `ErrorType` and `Message`, not the
original error value, so match on the `ErrorType` field rather than `errors.As` against
your own error types.

```go
--8<-- "examples/go/sdk-reference/languages/errors.go"
```

See [Error handling](../../error-handling/errors.md) for the full taxonomy and
[Wait](../../operations/wait.md), whose error never carries a business outcome.

## Static analysis with durablelint

The `github.com/aws/aws-durable-execution-sdk-go/analysis/durablelint` package provides
static analyzers that report determinism violations. The rules are:

- `durablegoroutine`, durable operations invoked on a goroutine the SDK did not start.
- `durablenestedop`, durable operations created inside a step body.
- `durablenondeterminism`, nondeterministic constructs in orchestration code outside a step
    body.
- `durablechildctx`, a captured context used inside a function that receives a context of
    its own.
- `durableclosure`, writes to captured variables inside a function whose result the SDK
    checkpoints.

The analyzers are in their own module,
`github.com/aws/aws-durable-execution-sdk-go/analysis`, which requires Go 1.25 or later.
Install the `durablelint` command and run it from your module root:

```console
go install github.com/aws/aws-durable-execution-sdk-go/analysis/cmd/durablelint@latest
durablelint ./...
```

It also runs as `go vet -vettool=$(which durablelint) ./...`, and golangci-lint v2 loads
the rules as a module plugin. Suppress a diagnostic with `//durable:ignore` on the
reported line or the line above it, or exclude a file with `//durable:ignore-file`. A
rule list after the comment, such as `//durable:ignore durablegoroutine`, narrows the
suppression to those rules.

## Testing with durabletest

The `durabletest` package runs a handler in process, without network access or AWS
credentials.

```go
--8<-- "examples/go/sdk-reference/languages/durabletest-signature.go"
```

`RunUntilComplete` invokes the handler as many times as the execution needs, completing
pending timers and step retries between invocations. The result's `Status` is
`durabletest.Succeeded` or `durabletest.Failed` when the execution ended, or
`durabletest.Pending` when it waits on external action, such as a callback. `ResultAs`
deserializes a succeeded result into the output type. The runner takes no test handle,
so the same calls work in a plain `main` program run with `go run`. See
[Testing](../../../testing/index.md) for the local runner, the cloud runner, and the
assertions.

## Logging and serialization

`Context.Logger()` and `StepContext.Logger()` return a replay-aware `*slog.Logger`.
Install your own `slog.Handler` with `durable.WithLogHandler`. See
[Logging](../../observability/logging.md). Operation results are serialized with
`encoding/json` by default. Callback results are returned as the submitted bytes by
default. See [Serialization](../../state/serialization.md) for `durable.WithSerdes`,
`durable.WithCallbackSerdes`, and the other options.

## Source and reference

The source and the SDK README are in
[aws/aws-durable-execution-sdk-go](https://github.com/aws/aws-durable-execution-sdk-go).
