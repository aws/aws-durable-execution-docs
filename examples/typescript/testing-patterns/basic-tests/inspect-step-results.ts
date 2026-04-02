import {
  LocalDurableTestRunner,
  OperationStatus,
} from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";
import { handler } from "./handler-with-error";

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

it("should fail when step throws an unretried error", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.FAILED);
  expect(result.getError().errorMessage).toBeDefined();
});
