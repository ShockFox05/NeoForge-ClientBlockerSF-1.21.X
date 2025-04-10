package net.ShockFox05.ClientBlockerSF.stub;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides stub implementations for client-only classes when running on a dedicated server.
 * This helps prevent crashes when other mods try to access client-only classes.
 */
public class StubClassTransformer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, byte[]> stubClasses = new HashMap<>();
    
    // Register stub classes here
    static {
        registerStubClasses();
    }
    
    /**
     * Register all stub classes that should be provided on the server.
     */
    private static void registerStubClasses() {
        // Register Minecraft client stubs
        registerStub("net.minecraft.client.gui.screens.Screen", ScreenStub.class);
        registerStub("net.minecraft.client.KeyMapping", KeyMappingStub.class);
        registerStub("com.mojang.blaze3d.vertex.BufferBuilder", BufferBuilderStub.class);
        
        // Add more stubs as needed
    }
    
    /**
     * Register a stub class for a client-only class.
     * 
     * @param clientClassName The fully qualified name of the client-only class
     * @param stubClass The stub implementation class
     */
    private static void registerStub(String clientClassName, Class<?> stubClass) {
        try {
            // Get the bytecode of the stub class
            String stubClassName = stubClass.getName();
            String resourceName = stubClassName.replace('.', '/') + ".class";
            byte[] bytecode = stubClass.getClassLoader().getResourceAsStream(resourceName).readAllBytes();
            
            // Store the bytecode with the client class name
            stubClasses.put(clientClassName, bytecode);
            LOGGER.info("[ClientBlockerSF] Registered stub for client class: " + clientClassName);
        } catch (Exception e) {
            LOGGER.error("[ClientBlockerSF] Failed to register stub for " + clientClassName, e);
        }
    }
    
    /**
     * Check if we have a stub for the requested class.
     * 
     * @param className The name of the class being requested
     * @return true if a stub is available
     */
    public static boolean hasStubFor(String className) {
        return stubClasses.containsKey(className);
    }
    
    /**
     * Get the bytecode for a stub class.
     * 
     * @param className The name of the client class
     * @return The bytecode for the stub class, or null if no stub is available
     */
    public static byte[] getStubBytecode(String className) {
        return stubClasses.get(className);
    }
}
