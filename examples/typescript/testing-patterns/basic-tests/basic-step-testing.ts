import {
  LocalDurableTestRunner,
  OperationStatus,
} from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";
import { handler } from "./add-numbers";

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

it("should execute a step and return the result", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe(8);
});
