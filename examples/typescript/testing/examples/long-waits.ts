import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationType } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  await context.wait("cooling-off", { hours: 24 });
  return await context.step("after-wait", () => "done");
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

it("completes instantly with time skipping enabled", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("done");

  const wait = runner.getOperation("cooling-off");
  expect(wait.getType()).toBe(OperationType.WAIT);
  expect(wait.getWaitDetails()?.waitSeconds).toBe(86400);
});
