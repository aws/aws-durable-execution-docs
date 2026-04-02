import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";
import { handler } from "./write-function";

beforeAll(async () => {
  await LocalDurableTestRunner.setupTestEnvironment();
});

afterAll(async () => {
  await LocalDurableTestRunner.teardownTestEnvironment();
});

test("my function", async () => {
  const runner = new LocalDurableTestRunner({ handlerFunction: handler });

  const result = await runner.run({ payload: { data: "test" } });

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("processed-test");
});
