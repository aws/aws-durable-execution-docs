import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner, WaitingOperationStatus } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  return await context.waitForCallback("approval", async (callbackId) => {
    // In production this would notify an external system
    void callbackId;
  });
});

let runner: LocalDurableTestRunner;

beforeAll(async () => {
  await LocalDurableTestRunner.setupTestEnvironment();
});

afterAll(async () => {
  await LocalDurableTestRunner.teardownTestEnvironment();
});

beforeEach(() => {
  runner = new LocalDurableTestRunner({ handlerFunction: handler });
});

it("completes a callback from the test", async () => {
  const runPromise = runner.run();

  const callback = runner.getOperation("approval");
  await callback.waitForData(WaitingOperationStatus.SUBMITTED);
  await callback.sendCallbackSuccess(JSON.stringify("approved"));

  const result = await runPromise;

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("approved");
});
