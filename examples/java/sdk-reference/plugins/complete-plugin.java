import software.amazon.lambda.durable.plugin.DurableExecutionPlugin;
import software.amazon.lambda.durable.plugin.InvocationInfo;
import software.amazon.lambda.durable.plugin.InvocationEndInfo;
import software.amazon.lambda.durable.plugin.OperationInfo;
import software.amazon.lambda.durable.plugin.OperationEndInfo;
import software.amazon.lambda.durable.plugin.UserFunctionStartInfo;
import software.amazon.lambda.durable.plugin.UserFunctionEndInfo;

public class ExamplePlugin implements DurableExecutionPlugin {
    @Override
    public void onInvocationStart(InvocationInfo info) {
        System.out.println("invocation start, arn: " + info.durableExecutionArn()
                + ", first invocation: " + info.isFirstInvocation());
    }

    @Override
    public void onInvocationEnd(InvocationEndInfo info) {
        System.out.println("invocation end, arn: " + info.durableExecutionArn()
                + ", status: " + info.invocationStatus());
    }

    @Override
    public void onOperationStart(OperationInfo info) {
        System.out.println("operation " + info.name() + " start, type: " + info.type());
    }

    @Override
    public void onOperationEnd(OperationEndInfo info) {
        System.out.println("operation " + info.name() + " end, error: " + info.error());
    }

    @Override
    public void onUserFunctionStart(UserFunctionStartInfo info) {
        System.out.println("user function " + info.name() + " start, attempt: " + info.attempt());
    }

    @Override
    public void onUserFunctionEnd(UserFunctionEndInfo info) {
        System.out.println("user function " + info.name() + " end, attempt: " + info.attempt()
                + ", succeeded: " + info.succeeded());
    }
}
