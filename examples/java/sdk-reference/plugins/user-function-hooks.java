@Override
public void onUserFunctionStart(UserFunctionStartInfo info) {
    System.out.println("user function " + info.name() + " start, attempt: " + info.attempt());
}

@Override
public void onUserFunctionEnd(UserFunctionEndInfo info) {
    System.out.println("user function " + info.name() + " end, attempt: " + info.attempt()
            + ", succeeded: " + info.succeeded());
}
