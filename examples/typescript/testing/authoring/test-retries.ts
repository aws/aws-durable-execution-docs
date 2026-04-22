import { withDurableExecution, DurableContext, createRetryStrategy } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

let attempts = 0;

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  return await context.step("flaky", () => {
    attempts++;
    if (attempts < 3) {
      throw new Error("transient error");
    }
    return "done";
  }, {
    retryStrategy: createRetryStrategy({ maxAttempts: 3 }),
  });
});

let runner: LocalDurableTestRunner;

beforeAll(async () => {
  await LocalDurableTestRunner.setupTestEnvironment({ skipTime: true });
});

afterAll(async () => {
  await LocalDurableTestRunner.teardownTestEnvironment();
});

beforeEach(() => {
  attempts = 0;
  runner = new LocalDurableTestRunner({ handlerFunction: handler });
});

it("retries and eventually succeeds", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("done");
});
