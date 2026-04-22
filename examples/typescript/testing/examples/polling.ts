import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { LocalDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

interface PollState {
  attempts: number;
  done: boolean;
}

const handler = withDurableExecution(async (event: unknown, context: DurableContext) => {
  return await context.waitForCondition<PollState>(
    "poll-job",
    async (state) => {
      const next = { attempts: state.attempts + 1, done: state.attempts >= 2 };
      return next;
    },
    {
      initialState: { attempts: 0, done: false },
      waitStrategy: (state) =>
        state.done
          ? { shouldContinue: false }
          : { shouldContinue: true, delay: { seconds: 1 } },
    },
  );
});

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

it("polls until condition is met", async () => {
  const result = await runner.run();

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult<PollState>()?.done).toBe(true);
});
