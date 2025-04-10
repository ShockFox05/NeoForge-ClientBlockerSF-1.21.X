package net.ShockFox05.ClientBlockerSF.stub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for tracking which stub classes have been loaded.
 */
public class StubClassRegistry {
    private static final ConcurrentHashMap<String, String> loadedStubs = new ConcurrentHashMap<>();
    
    /**
     * Register a stub class that has been loaded.
     * 
     * @param originalClassName The name of the original client-only class
     * @param stubClassName The name of the stub class that was provided
     */
    public static void registerLoadedStub(String originalClassName, String stubClassName) {
        loadedStubs.put(originalClassName, stubClassName);
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
}
