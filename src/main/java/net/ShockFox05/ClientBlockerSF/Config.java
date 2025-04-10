package net.ShockFox05.ClientBlockerSF;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ClientBlockerSF.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Configuration options
    public static final ModConfigSpec.BooleanValue ENABLE_STUB_CLASSES;
    public static final ModConfigSpec.BooleanValue LOG_STUB_LOADING;

    static {
        BUILDER.comment("Client Blocker SF Configuration");

        BUILDER.push("general");
        ENABLE_STUB_CLASSES = BUILDER
                .comment("Enable stub class system to prevent crashes when client-only classes are accessed on a server")
                .define("enableStubClasses", true);

        LOG_STUB_LOADING = BUILDER
                .comment("Log when stub classes are loaded")
                .define("logStubLoading", true);
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Loading event) {
        // Config has been loaded, update any runtime values if needed
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        // Config has been reloaded, update any runtime values if needed
    }
}
