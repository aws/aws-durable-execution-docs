import { Logger } from "@aws-lambda-powertools/logger";
import {
  createPowertoolsLogger,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";
import { createOtelPluginFactory } from "@aws/durable-execution-sdk-js-otel";

const logger = createPowertoolsLogger(new Logger({ serviceName: "orders" }));

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext) => {
    context.configureLogger({ customLogger: logger });
    context.logger.info("Processing order");
    return "processed";
  },
  { plugins: [createOtelPluginFactory()] },
);
