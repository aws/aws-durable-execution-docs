import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationType } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  return await context.runInChildContext("process", async (child) => {
    const a = await child.step("step-a", () => "result-a");
    const b = await child.step("step-b", () => "result-b");
    return `${a}:${b}`;
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

it("executes steps inside a child context", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("result-a:result-b");

  const ctx = runner.getOperation("process");
  expect(ctx.getType()).toBe(OperationType.CONTEXT);

  const children = ctx.getChildOperations();
  expect(children?.length).toBe(2);
  expect(children?.map(c => c.getName())).toEqual(["step-a", "step-b"]);
});
