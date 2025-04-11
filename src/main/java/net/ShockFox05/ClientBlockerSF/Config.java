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
    public static final ModConfigSpec.BooleanValue LOG_EXCEPTIONS;
    public static final ModConfigSpec.BooleanValue LOG_EXCEPTION_STACK_TRACES;
    public static final ModConfigSpec.BooleanValue ENABLE_COMPATIBILITY_FIXES;
    public static final ModConfigSpec.BooleanValue LOG_LOADED_STUBS;

    static {
        BUILDER.comment("Client Blocker SF Configuration");

        BUILDER.push("general");
        ENABLE_STUB_CLASSES = BUILDER
                .comment("Enable stub class system to prevent crashes when client-only classes are accessed on a server")
                .define("enableStubClasses", true);

        LOG_STUB_LOADING = BUILDER
                .comment("Log when stub classes are loaded")
                .define("logStubLoading", true);

        LOG_LOADED_STUBS = BUILDER
                .comment("Log all loaded stubs when the server starts")
                .define("logLoadedStubs", false);
        BUILDER.pop();

        BUILDER.push("compatibility");
        ENABLE_COMPATIBILITY_FIXES = BUILDER
                .comment("Enable compatibility fixes for known problematic mods")
                .define("enableCompatibilityFixes", true);
        BUILDER.pop();

        BUILDER.push("exceptions");
        LOG_EXCEPTIONS = BUILDER
                .comment("Log exceptions that are caught and handled by the mod")
                .define("logExceptions", true);

        LOG_EXCEPTION_STACK_TRACES = BUILDER
                .comment("Log stack traces for caught exceptions (can be verbose)")
                .define("logExceptionStackTraces", false);
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
