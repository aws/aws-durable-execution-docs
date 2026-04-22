import { CloudDurableTestRunner } from "@aws/durable-execution-sdk-js-testing";
import { LambdaClient } from "@aws-sdk/client-lambda";
import { ExecutionStatus } from "@aws-sdk/client-lambda";

const runner = new CloudDurableTestRunner({
  functionName: "MyFunction:$LATEST",
  client: new LambdaClient({ region: "us-east-1" }),
});

it("runs against a deployed function", async () => {
  const result = await runner.run({ payload: { name: "world" } });

  expect(result.getStatus()).toBe(ExecutionStatus.SUCCEEDED);
  expect(result.getResult()).toBe("hello world");
});
