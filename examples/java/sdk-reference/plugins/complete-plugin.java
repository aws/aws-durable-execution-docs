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
        System.out.println("invocation start " + info.durableExecutionArn()
                + " " + info.isFirstInvocation());
    }

    @Override
    public void onInvocationEnd(InvocationEndInfo info) {
        System.out.println("invocation end " + info.invocationStatus());
    }

    @Override
    public void onOperationStart(OperationInfo info) {
        System.out.println("operation start " + info.type() + " " + info.name());
    }

    @Override
    public void onOperationEnd(OperationEndInfo info) {
        System.out.println("operation end " + info.name());
    }

    @Override
    public void onUserFunctionStart(UserFunctionStartInfo info) {
        System.out.println("user function start " + info.name() + " " + info.attempt());
    }

    @Override
    public void onUserFunctionEnd(UserFunctionEndInfo info) {
        System.out.println("user function end " + info.name() + " " + info.succeeded());
    }
}
