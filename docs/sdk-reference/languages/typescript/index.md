# TypeScript SDK

The TypeScript SDK (`@aws/durable-execution-sdk-js`) runs in your Lambda functions and
provides `DurableContext`, operations, and the `withDurableExecution` wrapper.

## Installation

```console
npm install @aws/durable-execution-sdk-js
```

## Check orchestration code

The [Durable Execution ESLint plugin](https://github.com/aws/aws-durable-execution-sdk-js/tree/sdk-3.0.0/packages/aws-durable-execution-sdk-js-eslint-plugin)
checks durable orchestration patterns. Install it with its TypeScript parser:

```console
npm install --save-dev @aws/durable-execution-sdk-js-eslint-plugin @typescript-eslint/parser
```

The opt-in `require-durable-operation-in-child-context` rule reports a child-context
function that calls no durable operation. It is not included in `flat/recommended`.
Enable it with the README's
[Choose the rules yourself](https://github.com/aws/aws-durable-execution-sdk-js/blob/sdk-3.0.0/packages/aws-durable-execution-sdk-js-eslint-plugin/README.md#choose-the-rules-yourself)
configuration.

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
