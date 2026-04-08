import { handler } from "./basic-child-context";
import { createTests } from "@aws/durable-execution-sdk-js-testing";
import { OperationType, OperationStatus } from "@aws/durable-execution-sdk-js-testing";

createTests({
  handler,
  tests: (runner) => {
    it("returns the child context result", async () => {
      const execution = await runner.run({ orderId: "order-1" });
      expect(execution.getResult()).toEqual({ orderId: "order-1", charged: true, valid: true });
    });

    it("records a CONTEXT operation", async () => {
      const execution = await runner.run({ orderId: "order-1" });
      const contextOp = runner.getOperationByIndex(0);
      expect(contextOp.getType()).toBe(OperationType.CONTEXT);
      expect(contextOp.getStatus()).toBe(OperationStatus.SUCCEEDED);
    });

    it("child operations are nested under the context operation", async () => {
      const execution = await runner.run({ orderId: "order-1" });
      const contextOp = runner.getOperationByIndex(0);
      expect(contextOp.getChildOperations()).toHaveLength(2);
    });
  },
});
