import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  const results = await context.parallel("fetch-all", [
    async (ctx) => await ctx.step("fetch-a", () => "data-a"),
    async (ctx) => await ctx.step("fetch-b", () => "data-b"),
    async (ctx) => await ctx.step("fetch-c", () => "data-c"),
  ]);
  return results.getSucceeded();
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

it("executes branches in parallel and collects results", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toEqual(["data-a", "data-b", "data-c"]);
});
