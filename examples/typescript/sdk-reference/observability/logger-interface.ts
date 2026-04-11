import { DurableLogger, DurableLoggingContext } from "@aws/durable-execution-sdk-js";

// DurableLogger interface — implement this to use a custom logger.
interface DurableLogger {
  log?(level: "INFO" | "WARN" | "ERROR" | "DEBUG", ...params: unknown[]): void;
  error(...params: unknown[]): void;
  warn(...params: unknown[]): void;
  info(...params: unknown[]): void;
  debug(...params: unknown[]): void;
  configureDurableLoggingContext?(context: DurableLoggingContext): void;
}
