import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus, OperationType } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: { orderId: string }, context: DurableContext) => {
  const validated = await context.step("validate", () => ({ orderId: event.orderId, status: "validated" }));
  const paid = await context.step("payment", () => ({ ...validated, payment: "completed" }));
  const fulfilled = await context.step("fulfillment", () => ({ ...paid, fulfillment: "shipped" }));
  return fulfilled;
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

it("executes all steps in order", async () => {
  const result = await runner.run({ payload: { orderId: "order-123" } });

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);

  const ops = result.getOperations().filter(op => op.getType() === OperationType.STEP);
  expect(ops.length).toBe(3);
  expect(ops.map(op => op.getName())).toEqual(["validate", "payment", "fulfillment"]);
});
