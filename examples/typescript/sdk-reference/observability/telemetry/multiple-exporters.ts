import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";
import {
  DynamoDBExporter,
  EventBridgeExporter,
  S3Exporter,
  durableExecutionTelemetry,
} from "@aws/durable-execution-sdk-js-telemetry-exporter";

const telemetry = durableExecutionTelemetry({
  emitMode: "on-complete",
  samplingRate: 1.0,
  exporters: [
    new S3Exporter({ bucket: "durable-execution-telemetry-archive" }),
    new DynamoDBExporter({ tableName: "durable-execution-telemetry-live" }),
    new EventBridgeExporter({}),
  ],
});

export const handler = withDurableExecution(
  async (event: { id: string }, context: DurableContext) => {
    const data = await context.step("fetch-data", async () => event.id);
    await context.wait("cooldown", { seconds: 5 });
    return await context.step("process", async () => `processed ${data}`);
  },
  { plugins: [telemetry] },
);
