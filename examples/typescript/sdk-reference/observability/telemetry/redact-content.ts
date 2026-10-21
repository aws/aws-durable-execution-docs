import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";
import { durableExecutionTelemetry } from "@aws/durable-execution-sdk-js-telemetry-exporter";

const telemetry = durableExecutionTelemetry({
  samplingRate: 0.1,
  content: {
    input: (input) => ({ orderId: (input as { orderId?: string }).orderId }),
    output: (output) => ({ status: (output as { status?: string }).status }),
  },
});

export const handler = withDurableExecution(
  async (event: { orderId: string }, context: DurableContext) => {
    const order = await context.step("fetch-order", async () => event.orderId);
    await context.wait("cooldown", { seconds: 5 });
    return await context.step("process", async () => ({
      orderId: order,
      status: "processed",
    }));
  },
  { plugins: [telemetry] },
);
