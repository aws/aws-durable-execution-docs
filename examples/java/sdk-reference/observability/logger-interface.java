import org.slf4j.Logger;
import software.amazon.lambda.durable.logging.DurableLogger;

// DurableLogger wraps an SLF4J Logger and adds MDC entries:
//   durableExecutionArn, requestId, operationId, operationName, attempt
//
// Obtain it from any context:
//   DurableLogger logger = context.getLogger();
//   DurableLogger logger = stepContext.getLogger();
//
// Available methods:
//   logger.trace(String format, Object... args)
//   logger.debug(String format, Object... args)
//   logger.info(String format, Object... args)
//   logger.warn(String format, Object... args)
//   logger.error(String format, Object... args)
//   logger.error(String message, Throwable t)
