import {
  LocalDurableTestRunner,
  OperationType,
  OperationStatus,
} from "@aws/durable-execution-sdk-js-testing";
import { handler } from "./wait";

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

it("should complete wait operation", async () => {
  const result = await runner.run();

  expect(result.getResult()).toBe("Wait completed");

  const waitOp = runner.getOperationByIndex(0);
  expect(waitOp.getType()).toBe(OperationType.WAIT);
  expect(waitOp.getStatus()).toBe(OperationStatus.SUCCEEDED);
  expect(waitOp.getWaitDetails()?.waitSeconds).toBe(5);
  expect(waitOp.getWaitDetails()?.scheduledEndTimestamp).toBeInstanceOf(Date);
});
