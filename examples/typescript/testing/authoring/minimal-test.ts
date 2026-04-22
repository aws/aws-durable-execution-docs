import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import {
  LocalDurableTestRunner,
} from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  const result = await context.step("greet", () => "hello");
  return result;
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

it("returns the expected result", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("hello");
});
