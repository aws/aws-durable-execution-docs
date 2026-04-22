import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationType } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  return await context.runInChildContext("process", async (child) => {
    return await child.step("compute", () => 42);
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

it("asserts on child context and its operations", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);

  const ctx = runner.getOperation("process");
  expect(ctx.getType()).toBe(OperationType.CONTEXT);
  expect(ctx.getContextDetails()?.result).toBe(42);

  const children = ctx.getChildOperations();
  expect(children?.length).toBe(1);
  expect(children?.[0].getName()).toBe("compute");
});
