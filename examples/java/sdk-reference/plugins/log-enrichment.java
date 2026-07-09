@Override
public void onUserFunctionStart(UserFunctionStartInfo info) {
    MDC.put("operationName", info.name());
}

@Override
public void onUserFunctionEnd(UserFunctionEndInfo info) {
    MDC.remove("operationName");
}
