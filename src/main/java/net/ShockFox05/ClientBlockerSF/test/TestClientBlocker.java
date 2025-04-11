package net.ShockFox05.ClientBlockerSF.test;

import net.ShockFox05.ClientBlockerSF.ClientBlockerSF;
import net.ShockFox05.ClientBlockerSF.compat.ExceptionHandler;
import net.ShockFox05.ClientBlockerSF.stub.StubClassRegistry;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Test class for ClientBlockerSF functionality.
 */
public class TestClientBlocker {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Run a test to verify that the stub class system is working.
     */
    public static void runTest() {
        if (!FMLEnvironment.dist.isDedicatedServer()) {
            LOGGER.info("[{}] Not running on a dedicated server, skipping test", ClientBlockerSF.MOD_ID);
            return;
        }

        LOGGER.info("[{}] Running test to verify stub class system", ClientBlockerSF.MOD_ID);

        // Try to load a client-only class
        ExceptionHandler.execute(() -> {
            try {
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                LOGGER.info("[{}] Successfully loaded client class: {}", ClientBlockerSF.MOD_ID, minecraftClass.getName());

                // Check if it's a stub
                if (StubClassRegistry.isStubLoaded("net.minecraft.client.Minecraft")) {
                    LOGGER.info("[{}] Verified that the class is a stub", ClientBlockerSF.MOD_ID);
                } else {
                    LOGGER.warn("[{}] The class is not a stub!", ClientBlockerSF.MOD_ID);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("[{}] Failed to load client class: {}", ClientBlockerSF.MOD_ID, e.getMessage());
            }
        }, "testing stub class system");

        LOGGER.info("[{}] Test completed", ClientBlockerSF.MOD_ID);
    }
}
