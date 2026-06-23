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
