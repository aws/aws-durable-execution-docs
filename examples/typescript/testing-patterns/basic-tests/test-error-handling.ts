import {
  LocalDurableTestRunner,
  OperationStatus,
  OperationType,
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

it("should inspect step result by name", async () => {
  const stepOp = runner.getOperation("add_numbers");

  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);

  const details = stepOp.getStepDetails();
  expect(details?.result).toBeDefined();
  expect(stepOp.getStatus()).toBe(OperationStatus.SUCCEEDED);
});
