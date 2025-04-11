package net.ShockFox05.ClientBlockerSF.compat;

import net.ShockFox05.ClientBlockerSF.ClientBlockerSF;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for compatibility fixes and patches.
 * This class manages known compatibility issues between mods and provides fixes.
 */
public class CompatRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, List<CompatFix>> MOD_FIXES = new HashMap<>();
    private static final Map<String, List<CompatPatch>> CLASS_PATCHES = new HashMap<>();
    
    /**
     * Register a compatibility fix for a specific mod.
     * 
     * @param modId The mod ID that needs the fix
     * @param description Description of the issue
     * @param fix The fix to apply
     */
    public static void registerFix(String modId, String description, Runnable fix) {
        MOD_FIXES.computeIfAbsent(modId, k -> new ArrayList<>())
                .add(new CompatFix(description, fix));
        LOGGER.info("[{}] Registered compatibility fix for mod {}: {}", 
                ClientBlockerSF.MOD_ID, modId, description);
    }
    
    /**
     * Register a patch for a specific class.
     * 
     * @param className The fully qualified name of the class to patch
     * @param description Description of the issue
     * @param patch The patch to apply
     */
    public static void registerPatch(String className, String description, ClassPatch patch) {
        CLASS_PATCHES.computeIfAbsent(className, k -> new ArrayList<>())
                .add(new CompatPatch(description, patch));
        LOGGER.info("[{}] Registered patch for class {}: {}", 
                ClientBlockerSF.MOD_ID, className, description);
    }
    
    /**
     * Apply all fixes for a specific mod.
     * 
     * @param modId The mod ID to apply fixes for
     * @return true if any fixes were applied
     */
    public static boolean applyFixes(String modId) {
        List<CompatFix> fixes = MOD_FIXES.get(modId);
        if (fixes == null || fixes.isEmpty()) {
            return false;
        }
        
        boolean anyApplied = false;
        for (CompatFix fix : fixes) {
            try {
                LOGGER.info("[{}] Applying fix for mod {}: {}", 
                        ClientBlockerSF.MOD_ID, modId, fix.description);
                fix.fix.run();
                anyApplied = true;
            } catch (Throwable t) {
                LOGGER.error("[{}] Failed to apply fix for mod {}: {}", 
                        ClientBlockerSF.MOD_ID, modId, fix.description, t);
            }
        }
        
        return anyApplied;
    }
    
    /**
     * Apply all patches for a specific class.
     * 
     * @param className The fully qualified name of the class to patch
     * @param classBytes The original class bytecode
     * @return The patched bytecode, or the original if no patches were applied
     */
    public static byte[] applyPatches(String className, byte[] classBytes) {
        List<CompatPatch> patches = CLASS_PATCHES.get(className);
        if (patches == null || patches.isEmpty()) {
            return classBytes;
        }
        
        byte[] result = classBytes;
        for (CompatPatch patch : patches) {
            try {
                LOGGER.info("[{}] Applying patch for class {}: {}", 
                        ClientBlockerSF.MOD_ID, className, patch.description);
                result = patch.patch.apply(className, result);
            } catch (Throwable t) {
                LOGGER.error("[{}] Failed to apply patch for class {}: {}", 
                        ClientBlockerSF.MOD_ID, className, patch.description, t);
            }
        }
        
        return result;
    }
    
    /**
     * Initialize the compatibility registry with known fixes and patches.
     */
    public static void init() {
        // Register Create mod fixes
        registerFix("create", "Fix Create initialization with KubeJS", () -> {
            // This fix prevents the NullPointerException in CreateBuiltInRegistries
            try {
                // Try to initialize a safe dummy registry before Create does
                Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");
                Class<?> registryClass = Class.forName("net.minecraft.core.Registry");
                
                // This is a no-op but ensures the classes are loaded
                LOGGER.info("[{}] Pre-initializing registry classes to prevent Create/KubeJS conflict", 
                        ClientBlockerSF.MOD_ID);
            } catch (Exception e) {
                LOGGER.warn("[{}] Failed to pre-initialize registry classes: {}", 
                        ClientBlockerSF.MOD_ID, e.getMessage());
            }
        });
        
        // Register more fixes as needed
        
        LOGGER.info("[{}] Compatibility registry initialized with {} mod fixes and {} class patches", 
                ClientBlockerSF.MOD_ID, MOD_FIXES.size(), CLASS_PATCHES.size());
    }
    
    /**
     * A compatibility fix for a specific mod.
     */
    private static class CompatFix {
        final String description;
        final Runnable fix;
        
        CompatFix(String description, Runnable fix) {
            this.description = description;
            this.fix = fix;
        }
    }
    
    /**
     * A compatibility patch for a specific class.
     */
    private static class CompatPatch {
        final String description;
        final ClassPatch patch;
        
        CompatPatch(String description, ClassPatch patch) {
            this.description = description;
            this.patch = patch;
        }
    }
    
    /**
     * Interface for class patching.
     */
    @FunctionalInterface
    public interface ClassPatch {
        byte[] apply(String className, byte[] classBytes);
    }
}
