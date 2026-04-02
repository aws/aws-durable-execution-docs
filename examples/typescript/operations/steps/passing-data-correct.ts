import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

async function registerUser(email: string): Promise<string> {
  return `user-${email}`;
}

async function sendFollowUpEmail(userId: string): Promise<void> {
  // send email to user
}

// ✅ CORRECT: userId is returned from the step and restored from checkpoint on replay
export const handler = withDurableExecution(
  async (event: { email: string }, context: DurableContext) => {
    const userId = await context.step("register-user", async () => {
      return await registerUser(event.email);
    });

    await context.wait("follow-up-delay", { minutes: 10 });

    await context.step("send-follow-up-email", async () => {
      await sendFollowUpEmail(userId); // userId restored from checkpoint
    });
  },
);
