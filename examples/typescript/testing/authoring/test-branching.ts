import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: { premium: boolean }, context: DurableContext) => {
  if (event.premium) {
    return await context.step("premium-path", () => "premium");
  }
  return await context.step("standard-path", () => "standard");
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

it("takes the premium path", async () => {
  const result = await runner.run({ payload: { premium: true } });

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("premium");
});

it("takes the standard path", async () => {
  const result = await runner.run({ payload: { premium: false } });

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("standard");
});
