import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

async function registerUser(email: string): Promise<string> {
  return `user-${email}`;
}

async function sendFollowUpEmail(userId: string): Promise<void> {
  // send email to user
}

// ❌ WRONG: userId mutation is lost on replay after the wait
export const handler = withDurableExecution(
  async (event: { email: string }, context: DurableContext) => {
    let userId = "";

    await context.step("register-user", async () => {
      userId = await registerUser(event.email); // ⚠️ Lost on replay!
    });

    await context.wait("follow-up-delay", { minutes: 10 });

    await context.step("send-follow-up-email", async () => {
      await sendFollowUpEmail(userId); // userId is "" on replay
    });
  },
);
