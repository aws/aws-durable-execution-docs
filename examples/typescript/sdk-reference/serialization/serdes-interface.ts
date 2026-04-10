import { Serdes, SerdesContext } from "@aws/durable-execution-sdk-js";

// Serdes<T> interface
interface SerdesInterface<T> {
  serialize: (value: T | undefined, context: SerdesContext) => Promise<string | undefined>;
  deserialize: (data: string | undefined, context: SerdesContext) => Promise<T | undefined>;
}

// SerdesContext
interface SerdesContextShape {
  entityId: string;
  durableExecutionArn: string;
}
