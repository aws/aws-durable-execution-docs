import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner, OperationStatus } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationType } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  return await context.step("compute", () => 42);
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

it("asserts on a step operation", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);

  const step = runner.getOperation("compute");
  expect(step.getType()).toBe(OperationType.STEP);
  expect(step.getStatus()).toBe(OperationStatus.SUCCEEDED);
  expect(step.getStepDetails()?.result).toBe(42);
});
