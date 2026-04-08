import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

// WRONG - a and b share the parent counter, so their IDs depend on which promise
// resolves first. On replay, if the order differs, a gets b's checkpointed result
// and b gets a's.
export const wrongHandler = withDurableExecution(async (event: any, context: DurableContext) => {
  const a = context.step(async () => fetchA());
  const b = context.step(async () => fetchB());
  return { a: await a, b: await b };
});

// CORRECT - each branch has its own isolated counter, so IDs are stable regardless
// of completion order
export const handler = withDurableExecution(async (event: any, context: DurableContext) => {
  const a = context.runInChildContext(async (child) => child.step(async () => fetchA()));
  const b = context.runInChildContext(async (child) => child.step(async () => fetchB()));
  return { a: await a, b: await b };
});

async function fetchA() { return "result-a"; }
async function fetchB() { return "result-b"; }
