import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";
import { Logger } from "@aws-lambda-powertools/logger";

const powertoolsLogger = new Logger({ serviceName: "my-service" });

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  context.configureLogger({ customLogger: powertoolsLogger });
  context.logger.info("Using Powertools logger");
});
