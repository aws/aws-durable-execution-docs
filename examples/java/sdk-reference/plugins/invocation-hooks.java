@Override
public void onInvocationStart(InvocationInfo info) {
    System.out.println("invocation start " + info.durableExecutionArn()
            + " " + info.isFirstInvocation());
}

@Override
public void onInvocationEnd(InvocationEndInfo info) {
    System.out.println("invocation end " + info.invocationStatus());
}
