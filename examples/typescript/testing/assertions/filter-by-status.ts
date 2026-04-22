import { withDurableExecution, DurableContext, createRetryStrategy } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationStatus } from "@aws-sdk/client-lambda";

let callCount = 0;

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  return await context.step("flaky", () => {
    callCount++;
    if (callCount < 3) throw new Error("not yet");
    return "ok";
  }, { retryStrategy: createRetryStrategy({ maxAttempts: 3 }) });
});

let runner: LocalDurableTestRunner;

beforeAll(async () => {
  await LocalDurableTestRunner.setupTestEnvironment({ skipTime: true });
});

afterAll(async () => {
  await LocalDurableTestRunner.teardownTestEnvironment();
});

beforeEach(() => {
  callCount = 0;
  runner = new LocalDurableTestRunner({ handlerFunction: handler });
});

it("filters operations by status", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);

  const failedOps = result.getOperations({ status: OperationStatus.FAILED });
  expect(failedOps.length).toBe(2);

  const succeededOps = result.getOperations({ status: OperationStatus.SUCCEEDED });
  expect(succeededOps.length).toBe(1);
});
