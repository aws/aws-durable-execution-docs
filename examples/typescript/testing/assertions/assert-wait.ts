import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner, OperationStatus } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationType } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  await context.wait("my-wait", { seconds: 30 });
  return "done";
});

let runner: LocalDurableTestRunner;

beforeAll(async () => {
  await LocalDurableTestRunner.setupTestEnvironment({ skipTime: true });
});

afterAll(async () => {
  await LocalDurableTestRunner.teardownTestEnvironment();
});

beforeEach(() => {
  runner = new LocalDurableTestRunner({ handlerFunction: handler });
});

it("asserts on a wait operation", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);

  const wait = runner.getOperation("my-wait");
  expect(wait.getType()).toBe(OperationType.WAIT);
  expect(wait.getStatus()).toBe(OperationStatus.SUCCEEDED);
  expect(wait.getWaitDetails()?.waitSeconds).toBe(30);
  expect(wait.getWaitDetails()?.scheduledEndTimestamp).toBeInstanceOf(Date);
});
