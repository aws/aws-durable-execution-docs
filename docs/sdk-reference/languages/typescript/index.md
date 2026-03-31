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
