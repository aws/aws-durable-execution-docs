import {
  LocalDurableTestRunner,
  OperationStatus,
} from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";
import { handler } from "./unreliable-operation";

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

it("should retry and eventually succeed", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);

  const stepOp = runner.getOperation("unreliable_operation");
  expect(stepOp.getStatus()).toBe(OperationStatus.SUCCEEDED);
});
