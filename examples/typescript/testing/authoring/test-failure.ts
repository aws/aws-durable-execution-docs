import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: { fail: boolean }, context: DurableContext) => {
  if (event.fail) {
    throw new Error("intentional failure");
  }
  return "ok";
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

it("reports a failed execution", async () => {
  const result = await runner.run({ payload: { fail: true } });

  expect(result.getStatus()).toBe(ExecutionStatus.FAILED);
  expect(result.getError().errorMessage).toContain("intentional failure");
});
