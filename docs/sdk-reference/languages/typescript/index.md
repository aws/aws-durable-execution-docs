# TypeScript SDK

The TypeScript SDK (`@aws/durable-execution-sdk-js`) runs in your Lambda functions and
provides `DurableContext`, operations, and the `withDurableExecution` wrapper.

## Installation

```console
npm install @aws/durable-execution-sdk-js
```

## Lint durable functions with ESLint

The [Durable Execution ESLint plugin](https://github.com/aws/aws-durable-execution-sdk-js/tree/main/packages/aws-durable-execution-sdk-js-eslint-plugin)
reports code that breaks replay. Install it with the TypeScript parser:

```console
npm install --save-dev @aws/durable-execution-sdk-js-eslint-plugin @typescript-eslint/parser
```

Then add the plugin's recommended config to `eslint.config.js`, as its README shows.

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
