import { Logger } from '@aws-lambda-powertools/logger';
import { createPowertoolsLogger, DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

const powertoolsLogger = new Logger({ serviceName: 'my-service' });

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  context.configureLogger({ customLogger: createPowertoolsLogger(powertoolsLogger) });
  context.logger.info('Running handler');
});
