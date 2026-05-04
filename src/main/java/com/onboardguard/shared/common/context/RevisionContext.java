package com.onboardguard.shared.common.context;

public class RevisionContext {
    private static final ThreadLocal<String> currentAction = new ThreadLocal<>();

    public static void setCurrentAction(String action) {
        currentAction.set(action);
    }

    public static String getCurrentAction() {
        return currentAction.get();
    }

    public static void clear() {
        currentAction.remove();
    }
}
