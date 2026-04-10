import { withDurableExecution, Serdes, SerdesContext } from "@aws/durable-execution-sdk-js";

// A custom Serdes that throws on serialization failure.
// The SDK terminates the Lambda invocation when serialization fails.
const strictSerdes: Serdes<unknown> = {
  serialize: async (value: unknown, _context: SerdesContext) => {
    // Circular references cause JSON.stringify to throw
    return JSON.stringify(value);
  },
  deserialize: async (data: string | undefined, _context: SerdesContext) => {
    if (data === undefined) return undefined;
    return JSON.parse(data);
  },
};

export const handler = withDurableExecution(async (event, context) => {
  const result = await context.step(
    "build-result",
    async (stepCtx) => {
      // Return a value that can be serialized
      return { message: "hello", timestamp: Date.now() };
    },
    { serdes: strictSerdes },
  );
  return result;
});
