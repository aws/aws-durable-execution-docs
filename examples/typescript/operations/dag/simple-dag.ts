import {
  DurableContext,
  TaskHandle,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

type Joined = { users: number; orders: number };

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<Joined> => {
    let join!: TaskHandle<"join", Joined>;

    const result = await context.dag("load-and-join", (dag) => {
      const users = dag.step("fetch-users", [], async () => [
        { id: "u1" },
        { id: "u2" },
      ]);
      const orders = dag.step("fetch-orders", [], async () => [{ id: "o1" }]);

      join = dag.step("join", [users, orders], async (deps) => ({
        users: deps["fetch-users"].length,
        orders: deps["fetch-orders"].length,
      }));
    });

    return result.getResult(join)!;
  },
);
