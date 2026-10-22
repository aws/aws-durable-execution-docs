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
their dependencies without changing handler or durable operation calls.

### Upgrade dependencies together

Use these coordinated versions after publication:

| Package                                 | Version |
| --------------------------------------- | ------- |
| `@aws/durable-execution-sdk-js`         | `3.0.0` |
| `@aws/durable-execution-sdk-js-otel`    | `2.0.0` |
| `@aws/durable-execution-sdk-js-testing` | `1.3.2` |

If you use the Testing SDK, upgrade it first. Version `1.3.1` excludes Core 3 from its
peer range; `1.3.2` supports both Core 2 and Core 3. Then upgrade Core and OTel together.
OTel remains optional, but OTel 2 requires Core 3. Rebuild application bundles and update
any Lambda layers containing these packages.

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

### Validate the upgrade

Check the package versions resolved by the deployed function and its layers. Test a
workflow that suspends and resumes, then test concurrent invocations. Confirm that each
invocation gets separate plugin state, completed steps do not repeat their side effects,
and telemetry appears for both successful and failed executions. Preserve operation
names, order, nesting, and serialization while executions remain in flight.

For all API changes and before/after examples, see the
[TypeScript 2.x to 3.0 migration guide](https://github.com/aws/aws-durable-execution-sdk-js/blob/8740dffd3080e481a4e6cbd82106cd3f2e8d1ae5/docs/migration-2.x-to-3.0.md).
