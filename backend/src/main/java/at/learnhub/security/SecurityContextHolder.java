package at.learnhub.security;

/**
 * ThreadLocal holder for CustomSecurityContext
 * Workaround for RESTEasy proxy wrapping issue with @PermitAll endpoints
 */
public class SecurityContextHolder {
    private static final ThreadLocal<CustomSecurityContext> contextHolder = new ThreadLocal<>();

    public static void setContext(CustomSecurityContext context) {
        contextHolder.set(context);
    }

    public static CustomSecurityContext getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}