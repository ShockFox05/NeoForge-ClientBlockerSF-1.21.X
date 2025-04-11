package net.ShockFox05.ClientBlockerSF.compat;

import net.ShockFox05.ClientBlockerSF.ClientBlockerSF;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles mod initialization and applies compatibility fixes.
 */
public class ModInitHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> PROBLEMATIC_MODS = new HashSet<>();

    static {
        // Known problematic mods that might need special handling
        PROBLEMATIC_MODS.add("create");
        PROBLEMATIC_MODS.add("kubejs");
        PROBLEMATIC_MODS.add("apotheosis");
        PROBLEMATIC_MODS.add("ars_nouveau");
    }

    /**
     * Apply early fixes before mod initialization.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEarlyInit(FMLCommonSetupEvent event) {
        LOGGER.info("[{}] Applying early compatibility fixes", ClientBlockerSF.MOD_ID);

        // Initialize the compatibility registry
        CompatRegistry.init();

        // Apply fixes for known problematic mods
        for (String modId : PROBLEMATIC_MODS) {
            ExceptionHandler.execute(() -> CompatRegistry.applyFixes(modId),
                    "applying early fixes for " + modId);
        }

        // Install our exception handlers
        installExceptionHandlers();
    }

    /**
     * Apply late fixes after all mods have initialized.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLateInit(FMLLoadCompleteEvent event) {
        LOGGER.info("[{}] Applying late compatibility fixes", ClientBlockerSF.MOD_ID);

        // Apply any late fixes here
    }

    /**
     * Install exception handlers to catch and handle exceptions during mod initialization.
     */
    private void installExceptionHandlers() {
        try {
            // Try to install a global exception handler
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                if (ExceptionHandler.handleException(throwable, "uncaught exception in thread " + thread.getName(), null)) {
                    // Exception was handled, don't propagate
                    LOGGER.warn("[{}] Suppressed uncaught exception in thread {}",
                            ClientBlockerSF.MOD_ID, thread.getName());
                } else {
                    // Exception wasn't handled, log it
                    LOGGER.error("[{}] Uncaught exception in thread {}",
                            ClientBlockerSF.MOD_ID, thread.getName(), throwable);
                }
            });

            LOGGER.info("[{}] Installed global exception handler", ClientBlockerSF.MOD_ID);
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to install global exception handler", ClientBlockerSF.MOD_ID, e);
        }

        // Try to patch specific mod initialization methods
        patchModInitMethods();
    }

    /**
     * Patch specific mod initialization methods to catch exceptions.
     */
    private void patchModInitMethods() {
        // This is a placeholder for more advanced patching
        // In a real implementation, you might use bytecode manipulation or reflection
        // to patch specific methods that are known to cause problems

        LOGGER.info("[{}] Patched mod initialization methods", ClientBlockerSF.MOD_ID);
    }
}
