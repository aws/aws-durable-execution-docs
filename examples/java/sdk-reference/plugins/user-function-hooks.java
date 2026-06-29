@Override
public void onUserFunctionStart(UserFunctionStartInfo info) {
    System.out.println("user function start " + info.name() + " " + info.attempt());
}

@Override
public void onUserFunctionEnd(UserFunctionEndInfo info) {
    System.out.println("user function end " + info.name() + " " + info.succeeded());
}
