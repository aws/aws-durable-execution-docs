// The step return value is checkpointed as durable state.
const value = await context.step("fetch", async () => fetchValue());

// Assigning or copying the local variable does NOT add to durable state.
const alias = value;
const copy = structuredClone(value);

// Returning the local variable from the handler DOES add it to durable state
// (the handler output is serialized and stored).
return { value };
