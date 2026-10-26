# TypeScript SDK

The TypeScript SDK (`@aws/durable-execution-sdk-js`) runs in your Lambda functions and
provides `DurableContext`, operations, and the `withDurableExecution` wrapper.

## Installation

```console
npm install @aws/durable-execution-sdk-js
```

## Usage

Wrap your Lambda handler with `withDurableExecution`:

```typescript
import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result = await context.step(async () => {
      return "step completed";
    });
    return result;
  },
);
```

The TypeScript SDK uses `async/await` throughout.

## Upgrade from 2.x to 3.0

Core 3.0 creates a fresh instrumentation plugin for each Lambda invocation. This isolates
plugin state when invocations run concurrently. Applications that register no plugins,
select none through `DURABLE_EXECUTION_PLUGINS`, and ship no plugin providers can update
their dependencies without changing handler or durable operation calls. Review the
behavior changes below even if your application uses no plugins.

### Upgrade dependencies together

The [Core 3.0 and OTel 2.0 release](https://github.com/aws/aws-durable-execution-sdk-js/releases/tag/sdk-3.0.0)
is available on npm and requires Node.js 22 or later. Use these coordinated versions:

| Package                                 | Version |
| --------------------------------------- | ------- |
| `@aws/durable-execution-sdk-js`         | `3.0.0` |
| `@aws/durable-execution-sdk-js-otel`    | `2.0.0` |
| `@aws/durable-execution-sdk-js-testing` | `1.3.2` |

If you use the Testing SDK, upgrade it first. Version `1.3.1` excludes Core 3 from its
peer range; `1.3.2` supports both Core 2 and Core 3. Then upgrade Core and OTel together.
OTel remains optional, but OTel 2 requires Core `>=3.0.0 <4.0.0`. Regenerate the lock file,
rebuild application bundles, and update any Lambda layers containing these packages.

### Update plugin registrations

Replace plugin instances in `plugins` with factory objects. Each factory's synchronous
`createPlugin(info)` method must return a fresh plugin. Keep providers and exporters
shared where appropriate, and keep invocation state on the new plugin.

Replace `new InvocationOtelPlugin(...)` with `createInvocationOtelPluginFactory(...)`,
or `new ExecutionOtelPlugin(...)` with `createExecutionOtelPluginFactory(...)`. Choose
one OTel view across code and environment configuration.

Environment-loaded modules still export `durableExecutionPluginProvider`. Change that
export to the factory contract and remove the legacy `pluginApiVersion` and `pluginType`
fields. Rebuild custom provider packages and their layers.

### Update custom operation hooks

Core 3 no longer awaits promises returned by `onOperationStart`, `onOperationEnd`,
`onOperationAttemptStart`, `onOperationAttemptEnd`, or `onOperationChange` before
continuing. Set up state needed by later hooks synchronously. Queue asynchronous work
explicitly when its order matters, and return its promise so the SDK can track it.

The SDK drains earlier hook promises until 500 ms before the invocation deadline, then
waits for `onInvocationEnd` until 50 ms before the deadline. Flush buffered work in
`onInvocationEnd` and tolerate incomplete earlier work. These limits stop waiting; they
do not cancel work or guarantee delivery. Without a usable deadline, the SDK waits for
the promises to settle. `onInvocationStart` remains awaited.

### Review other behavior changes

The OTel execution view reports `faas.max_memory` in bytes instead of MiB. A 1024 MiB
function now reports `1073741824` instead of `1024`. Update queries, dashboards, and alarms
that read this attribute.

A failed `context.invoke` still throws `InvokeError`, but its `cause.name` now preserves
the invoked function's error type instead of always containing `"Error"`. Review code
that branches on this value.

An operation remains `EXECUTING` until its result is deserialized. A slow deserializer
failure now fails the invocation; a deserializer that never settles can hold it open
until the Lambda timeout.

The idle suspension delay changes from 20 ms to 3 ms. The SDK does not track an `await`
between durable operations, so slow work there can let an invocation suspend before the
next operation starts. Move that work into a step.

### Validate the upgrade

Check the package versions resolved by the deployed function and its layers. Test a
workflow that suspends and resumes, then test concurrent invocations. Confirm that each
invocation gets separate plugin state, completed steps do not repeat their side effects,
and telemetry appears for both successful and failed executions. Preserve operation
names, order, nesting, and serialization while executions remain in flight.

Test slow operation hooks and invocation-end flushing near the deadline. If you use the
Testing SDK, review invocation-count and history-order assertions against the scheduling
fixes in the release notes.

For all API changes and before/after examples, see the
[TypeScript 2.x to 3.0 migration guide](https://github.com/aws/aws-durable-execution-sdk-js/blob/sdk-3.0.0/docs/migration-2.x-to-3.0.md).
