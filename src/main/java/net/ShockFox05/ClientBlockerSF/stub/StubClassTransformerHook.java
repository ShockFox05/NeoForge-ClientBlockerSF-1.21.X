package net.ShockFox05.ClientBlockerSF.stub;

import net.ShockFox05.ClientBlockerSF.Config;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Hooks into the class loading system to provide stub implementations for client-only classes.
 */
public class StubClassTransformerHook {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;

    /**
     * Initialize the stub class transformer.
     * This should be called early in the mod initialization process.
     */
    public static void init() {
        if (initialized) {
            return;
        }

        // Only apply on dedicated servers
        if (FMLEnvironment.dist.isDedicatedServer()) {
            try {
                LOGGER.info("[ClientBlockerSF] Initializing stub class transformer for dedicated server");

                // Create and register our custom class loader
                ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
                StubClassLoader stubLoader = new StubClassLoader(currentLoader);
                Thread.currentThread().setContextClassLoader(stubLoader);

                LOGGER.info("[ClientBlockerSF] Stub class transformer initialized successfully");
            } catch (Exception e) {
                LOGGER.error("[ClientBlockerSF] Failed to initialize stub class transformer", e);
            }
        }

        initialized = true;
    }

    /**
     * Check if the stub system should be enabled based on config.
     * This should only be called after the config is loaded.
     */
    public static boolean isEnabled() {
        try {
            return Config.ENABLE_STUB_CLASSES.get();
        } catch (Exception e) {
            // If config isn't loaded yet, default to enabled
            return true;
        }
    }

    /**
     * Check if stub loading should be logged based on config.
     * This should only be called after the config is loaded.
     */
    public static boolean shouldLogLoading() {
        try {
            return Config.LOG_STUB_LOADING.get();
        } catch (Exception e) {
            // If config isn't loaded yet, default to true
            return true;
        }
    }
}
