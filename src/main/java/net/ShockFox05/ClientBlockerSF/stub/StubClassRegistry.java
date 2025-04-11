package net.ShockFox05.ClientBlockerSF.stub;

import net.ShockFox05.ClientBlockerSF.ClientBlockerSF;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for tracking which stub classes have been loaded.
 */
public class StubClassRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentHashMap<String, String> loadedStubs = new ConcurrentHashMap<>();
    private static final Set<String> failedStubs = new HashSet<>();

    /**
     * Register a stub class that has been loaded.
     *
     * @param originalClassName The name of the original client-only class
     * @param stubClassName The name of the stub class that was provided
     */
    public static void registerLoadedStub(String originalClassName, String stubClassName) {
        LOGGER.debug("[{}] Registered loaded stub: {} -> {}", ClientBlockerSF.MOD_ID, originalClassName, stubClassName);
        loadedStubs.put(originalClassName, stubClassName);
    }

    /**
     * Register a failed stub loading attempt.
     *
     * @param className The name of the class that failed to load
     */
    public static void registerFailedStub(String className) {
        LOGGER.debug("[{}] Registered failed stub: {}", ClientBlockerSF.MOD_ID, className);
        failedStubs.add(className);
    }

    /**
     * Get a list of all loaded stub classes.
     *
     * @return A list of strings in the format "originalClassName -> stubClassName"
     */
    public static List<String> getLoadedStubs() {
        List<String> result = new ArrayList<>();
        loadedStubs.forEach((original, stub) -> {
            result.add(original + " -> " + stub);
        });
        return result;
    }

    /**
     * Check if a stub has been loaded for a specific class.
     *
     * @param originalClassName The name of the original client-only class
     * @return true if a stub has been loaded for this class
     */
    public static boolean isStubLoaded(String originalClassName) {
        return loadedStubs.containsKey(originalClassName);
    }

    /**
     * Check if a stub failed to load for a class.
     *
     * @param className The name of the class to check
     * @return true if a stub failed to load
     */
    public static boolean hasStubFailed(String className) {
        return failedStubs.contains(className);
    }

    /**
     * Get the name of the stub class that was loaded for a class.
     *
     * @param className The name of the original class
     * @return The name of the stub class, or null if no stub was loaded
     */
    public static String getLoadedStubName(String className) {
        return loadedStubs.get(className);
    }

    /**
     * Get the number of stubs that have been loaded.
     *
     * @return The number of loaded stubs
     */
    public static int getLoadedStubCount() {
        return loadedStubs.size();
    }

    /**
     * Get the number of stubs that failed to load.
     *
     * @return The number of failed stubs
     */
    public static int getFailedStubCount() {
        return failedStubs.size();
    }
}
