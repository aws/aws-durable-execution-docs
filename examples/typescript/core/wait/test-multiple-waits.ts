import {
  LocalDurableTestRunner,
  OperationType,
  OperationStatus,
} from "@aws/durable-execution-sdk-js-testing";
import { handler } from "./multiple-waits";

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

it("should handle multiple sequential wait operations", async () => {
  const firstWait = runner.getOperation("wait-1");
  const secondWait = runner.getOperation("wait-2");

  const result = await runner.run();

  expect(result.getResult()).toEqual({ completedWaits: 2, finalStep: "done" });

  const operations = result.getOperations();
  expect(operations.length).toEqual(2);

  expect(firstWait.getType()).toBe(OperationType.WAIT);
  expect(firstWait.getStatus()).toBe(OperationStatus.SUCCEEDED);
  expect(firstWait.getWaitDetails()?.waitSeconds).toBe(5);

  expect(secondWait.getType()).toBe(OperationType.WAIT);
  expect(secondWait.getStatus()).toBe(OperationStatus.SUCCEEDED);
  expect(secondWait.getWaitDetails()?.waitSeconds).toBe(5);
});
