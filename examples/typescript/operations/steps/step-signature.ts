import { DurableContext, StepConfig, StepFunc } from "@aws/durable-execution-sdk-js";

// Sync signature (unnamed)
context.step(fn: StepFunc<T>, config?: StepConfig<T>): DurablePromise<T>

// Named signature
context.step(name: string | undefined, fn: StepFunc<T>, config?: StepConfig<T>): DurablePromise<T>
