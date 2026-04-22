import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationStatus } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  await context.step("step-1", () => "ok");
  await context.step("step-2", () => "ok");
  await context.step("step-3", () => { throw new Error("step-3 failed"); });
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

it("records which steps succeeded before the failure", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.FAILED);

  const step1 = runner.getOperation("step-1");
  expect(step1.getStatus()).toBe(OperationStatus.SUCCEEDED);

  const step2 = runner.getOperation("step-2");
  expect(step2.getStatus()).toBe(OperationStatus.SUCCEEDED);

  expect(result.getError().errorMessage).toContain("step-3 failed");
});
