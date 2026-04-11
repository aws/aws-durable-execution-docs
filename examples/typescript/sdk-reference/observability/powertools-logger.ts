import { Logger } from '@aws-lambda-powertools/logger';
import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

const powertoolsLogger = new Logger({ serviceName: 'my-service' });

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  context.configureLogger({ customLogger: powertoolsLogger });
  context.logger.info('Running handler');
});
