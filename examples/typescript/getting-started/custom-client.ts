import { LambdaClient } from "@aws-sdk/client-lambda";
import {
  DurableContext,
  withDurableExecution,
  DurableExecutionConfig,
} from "@aws/durable-execution-sdk-js";

const customClient = new LambdaClient({
  region: "us-west-2",
  maxAttempts: 5,
  retryMode: "adaptive",
});

const config: DurableExecutionConfig = { client: customClient };

export const handler = withDurableExecution(
  async (event: Record<string, unknown>, context: DurableContext) => {
    // Your durable function logic
    return { status: "success" };
  },
  config,
);
