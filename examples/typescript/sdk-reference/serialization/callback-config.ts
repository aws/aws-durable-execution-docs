import {
  DurableContext,
  Serdes,
  SerdesContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

interface ApprovalResult {
  approved: boolean;
  reason: string;
}

// Callbacks only need deserialization — the external system sends the payload.
const approvalSerdes: Omit<Serdes<ApprovalResult>, "serialize"> = {
  deserialize: async (data: string | undefined, _ctx: SerdesContext) =>
    data !== undefined ? (JSON.parse(data) as ApprovalResult) : undefined,
};

export const handler = withDurableExecution(
  async (event: unknown, context: DurableContext) => {
    const [approval, callbackId] = context.createCallback("await-approval", {
      serdes: approvalSerdes,
    });

    // Send callbackId to the external system here.
    console.log("Callback ID:", callbackId);

    const result = await approval;
    return result;
  },
);
