package net.ShockFox05.ClientBlockerSF.stub;

import net.ShockFox05.ClientBlockerSF.ClientBlockerSF;
import net.ShockFox05.ClientBlockerSF.compat.CompatRegistry;
import net.ShockFox05.ClientBlockerSF.compat.ExceptionHandler;
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
        registerStub("net.minecraft.client.model.geom.ModelPart", ModelRendererStub.class);
        registerStub("net.minecraft.client.renderer.RenderSystem", RenderSystemStub.class);
        registerStub("net.minecraft.client.resources.model.ModelResourceLocation", ResourceLocationStub.class);

        // Add more stubs from our mapping
        for (String className : StubClassMapping.getAllMappedClasses()) {
            if (!stubClasses.containsKey(className)) {
                Class<?> stubClass = StubClassMapping.getStubClass(className);
                if (stubClass != null && stubClass != Object.class) {
                    registerStub(className, stubClass);
                }
            }
        }
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
            LOGGER.info("[{}] Registered stub for client class: {}", ClientBlockerSF.MOD_ID, clientClassName);
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to register stub for {}", ClientBlockerSF.MOD_ID, clientClassName, e);
        }
    }

    /**
     * Check if we have a stub for the requested class.
     *
     * @param className The name of the class being requested
     * @return true if a stub is available
     */
    public static boolean hasStubFor(String className) {
        return stubClasses.containsKey(className) || StubClassMapping.isClientClass(className);
    }

    /**
     * Get the bytecode for a stub class.
     *
     * @param className The name of the client class
     * @return The bytecode for the stub class, or null if no stub is available
     */
    public static byte[] getStubBytecode(String className) {
        // First check if we have a pre-registered stub
        byte[] bytecode = stubClasses.get(className);
        if (bytecode != null) {
            return bytecode;
        }

        // If not, check if we can generate one on-the-fly
        if (StubClassMapping.isClientClass(className)) {
            // Get the stub class or use Object as fallback
            final Class<?> finalStubClass = StubClassMapping.getStubClass(className) != null ?
                    StubClassMapping.getStubClass(className) : Object.class;

            // Generate stub bytecode
            return ExceptionHandler.executeWithReturn(
                    () -> StubClassLoader.generateStubClassBytes(className, finalStubClass),
                    "generating stub for " + className,
                    null);
        }

        return null;
    }

    /**
     * Transform a class if needed (apply patches or provide stubs).
     *
     * @param name The name of the class being loaded
     * @param transformedName The transformed name of the class being loaded
     * @param basicClass The class bytes
     * @return The transformed class bytes, or null if no transformation was done
     */
    public static byte[] transform(String name, String transformedName, byte[] basicClass) {
        // First, apply any compatibility patches
        byte[] patchedClass = ExceptionHandler.executeWithReturn(
                () -> CompatRegistry.applyPatches(transformedName, basicClass),
                "applying patches to " + transformedName,
                basicClass);

        // Check if this is a client-only class that we have a stub for
        if (hasStubFor(transformedName)) {
            LOGGER.info("[{}] Transforming client-only class: {}", ClientBlockerSF.MOD_ID, transformedName);

            // Get the stub bytecode
            byte[] stubBytes = getStubBytecode(transformedName);
            if (stubBytes != null) {
                // Register the loaded stub
                StubClassRegistry.registerLoadedStub(transformedName, "stub");
                return stubBytes;
            } else {
                LOGGER.warn("[{}] Failed to get stub for: {}", ClientBlockerSF.MOD_ID, transformedName);
            }
        }

        return patchedClass != basicClass ? patchedClass : null;
    }
}
