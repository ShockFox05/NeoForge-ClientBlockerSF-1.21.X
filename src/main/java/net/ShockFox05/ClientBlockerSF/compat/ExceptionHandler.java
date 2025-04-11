package net.ShockFox05.ClientBlockerSF.compat;

import net.ShockFox05.ClientBlockerSF.ClientBlockerSF;
import net.ShockFox05.ClientBlockerSF.Config;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles exceptions that would normally crash the server.
 * This class provides mechanisms to intercept, log, and recover from exceptions.
 */
public class ExceptionHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Class<? extends Throwable>, ExceptionHandler> HANDLERS = new HashMap<>();

    private final String exceptionName;
    private final String description;
    private final boolean shouldSuppress;

    static {
        // Register handlers for common exceptions
        register(NullPointerException.class, "NullPointerException",
                "Occurs when a null reference is used where an object is required", true);
        register(ClassNotFoundException.class, "ClassNotFoundException",
                "Occurs when trying to load a class that doesn't exist", true);
        register(NoClassDefFoundError.class, "NoClassDefFoundError",
                "Occurs when a required class definition cannot be found", true);
        register(ExceptionInInitializerError.class, "ExceptionInInitializerError",
                "Occurs when an exception is thrown during static initialization", true);
        register(IllegalAccessError.class, "IllegalAccessError",
                "Occurs when trying to access a class, field, or method that is not accessible", true);
        register(LinkageError.class, "LinkageError",
                "Occurs when a class has incompatible dependencies", true);
    }

    private ExceptionHandler(String exceptionName, String description, boolean shouldSuppress) {
        this.exceptionName = exceptionName;
        this.description = description;
        this.shouldSuppress = shouldSuppress;
    }

    /**
     * Register a handler for a specific exception type.
     */
    public static <T extends Throwable> void register(Class<T> exceptionClass, String name, String description, boolean suppress) {
        HANDLERS.put(exceptionClass, new ExceptionHandler(name, description, suppress));
    }

    /**
     * Execute code and handle any exceptions that occur.
     *
     * @param code The code to execute
     * @param context A description of what was being attempted
     * @return true if execution completed without exceptions or if the exception was handled
     */
    public static boolean execute(Runnable code, String context) {
        try {
            code.run();
            return true;
        } catch (Throwable t) {
            return handleException(t, context, null);
        }
    }

    /**
     * Execute code that returns a value and handle any exceptions that occur.
     *
     * @param code The code to execute
     * @param context A description of what was being attempted
     * @param defaultValue The default value to return if an exception occurs
     * @return The result of the code or the default value if an exception occurred
     */
    public static <T> T executeWithReturn(Supplier<T> code, String context, T defaultValue) {
        try {
            return code.get();
        } catch (Throwable t) {
            handleException(t, context, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Handle an exception that has already been caught.
     *
     * @param t The exception to handle
     * @param context A description of what was being attempted
     * @param defaultValue The default value that will be returned (for logging purposes)
     * @return true if the exception was handled and execution should continue
     */
    public static boolean handleException(Throwable t, String context, Object defaultValue) {
        // Find the root cause
        Throwable rootCause = t;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        // Find a handler for this exception type
        ExceptionHandler handler = findHandler(rootCause.getClass());

        // Log the exception
        try {
            boolean logExceptions = true;
            boolean logStackTraces = false;

            try {
                // Try to get config values, but don't crash if they're not loaded yet
                logExceptions = Config.LOG_EXCEPTIONS.get();
                logStackTraces = Config.LOG_EXCEPTION_STACK_TRACES.get();
            } catch (IllegalStateException e) {
                // Config not loaded yet, use defaults
                LOGGER.debug("[{}] Config not loaded yet, using default logging settings", ClientBlockerSF.MOD_ID);
            }

            if (logExceptions) {
                if (handler != null) {
                    LOGGER.warn("[{}] {} during {}: {} - {}",
                            ClientBlockerSF.MOD_ID, handler.exceptionName, context, rootCause.getMessage(), handler.description);

                    if (logStackTraces) {
                        LOGGER.debug("Stack trace:", rootCause);
                    }
                } else {
                    LOGGER.error("[{}] Unhandled exception during {}: {}",
                            ClientBlockerSF.MOD_ID, context, rootCause.getMessage(), rootCause);
                }

                if (defaultValue != null) {
                    LOGGER.info("[{}] Using default value: {}", ClientBlockerSF.MOD_ID, defaultValue);
                }
            }
        } catch (Exception e) {
            // Fallback if anything goes wrong with logging
            LOGGER.error("[{}] Error while handling exception: {}", ClientBlockerSF.MOD_ID, e.getMessage());
            LOGGER.error("[{}] Original exception: {}", ClientBlockerSF.MOD_ID, rootCause.getMessage());
        }

        // Determine if we should suppress this exception
        return handler != null && handler.shouldSuppress;
    }

    /**
     * Find a handler for the given exception type or its superclasses.
     */
    private static ExceptionHandler findHandler(Class<? extends Throwable> exceptionClass) {
        // Check for an exact match
        ExceptionHandler handler = HANDLERS.get(exceptionClass);
        if (handler != null) {
            return handler;
        }

        // Check for a match with a superclass
        for (Map.Entry<Class<? extends Throwable>, ExceptionHandler> entry : HANDLERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionClass)) {
                return entry.getValue();
            }
        }

        return null;
    }
}
