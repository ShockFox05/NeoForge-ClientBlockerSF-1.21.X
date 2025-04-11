package net.ShockFox05.ClientBlockerSF;

import com.mojang.brigadier.CommandDispatcher;
import net.ShockFox05.ClientBlockerSF.compat.CompatRegistry;
import net.ShockFox05.ClientBlockerSF.compat.ExceptionHandler;
import net.ShockFox05.ClientBlockerSF.compat.ModInitHandler;
import net.ShockFox05.ClientBlockerSF.stub.StubClassMapping;
import net.ShockFox05.ClientBlockerSF.test.TestClientBlocker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.ShockFox05.ClientBlockerSF.stub.StubClassRegistry;
import net.ShockFox05.ClientBlockerSF.stub.StubClassTransformerHook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod(ClientBlockerSF.MOD_ID)
public class ClientBlockerSF {
    public static final String MOD_ID = "clientblockersf";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Make the client feature accessible to the mod.
    public static final IClientFeature CLIENT_FEATURE;

    static {
        // Choose the client-specific implementation only if we're not on a dedicated server.
        if (FMLEnvironment.dist.isDedicatedServer()) {
            CLIENT_FEATURE = new NoOpClientFeature();
        } else {
            CLIENT_FEATURE = new ClientFeatureImpl();
        }

        // Register command listener as before.
        NeoForge.EVENT_BUS.addListener(CommandRegistrationHandler::registerCommands);
    }

    public ClientBlockerSF(IEventBus modEventBus) {
        LOGGER.info("[{}] Initializing...", MOD_ID);

        // Register config
        modEventBus.register(Config.class);
        modEventBus.addListener(this::onConfigLoad);

        // Register server event listeners
        NeoForge.EVENT_BUS.register(this);

        // Create and register mod initialization handler
        ModInitHandler initHandler = new ModInitHandler();
        modEventBus.register(initHandler);

        // Initialize stub class transformer early
        if (FMLEnvironment.dist.isDedicatedServer()) {
            ExceptionHandler.execute(() -> StubClassTransformerHook.init(), "initializing stub class transformer hook");

            // Initialize compatibility registry
            ExceptionHandler.execute(() -> CompatRegistry.init(), "initializing compatibility registry");
        }

        // Invoke client-specific setup (this call is a no-op on a dedicated server).
        CLIENT_FEATURE.performClientSetup();

        LOGGER.info("[{}] Initialized successfully!", MOD_ID);
    }

    private void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(MOD_ID)) {
            LOGGER.info("[ClientBlockerSF] Config loaded");
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            LOGGER.info("[{}] Running on dedicated server. Scanning for client-only classes...", MOD_ID);

            // Get client-only classes from our mapping
            Set<String> clientOnlyClasses = StubClassMapping.getAllMappedClasses();
            List<String> sampleClasses = List.of(
                    "net.minecraft.client.Minecraft",
                    "net.minecraft.client.gui.screens.Screen",
                    "com.mojang.blaze3d.vertex.RenderSystem",
                    "net.minecraft.client.KeyMapping",
                    "com.mojang.blaze3d.vertex.BufferBuilder"
            );

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (String className : sampleClasses) {
                ExceptionHandler.execute(() -> {
                    String resourceName = className.replace('.', '/') + ".class";
                    if (classLoader.getResource(resourceName) != null) {
                        LOGGER.warn("[{}] WARNING: Client-only class '{}' is present on server. Stub implementation will be provided.",
                                MOD_ID, className);
                    } else {
                        LOGGER.info("[{}] Verified: {} is not present on server (as expected).",
                                MOD_ID, className);
                    }
                }, "checking for client class " + className);
            }

            // Log stub class statistics
            LOGGER.info("[{}] Stub class system is active with {} mapped client classes.",
                    MOD_ID, clientOnlyClasses.size());
            LOGGER.info("[{}] Currently loaded {} stub classes, {} failed.",
                    MOD_ID, StubClassRegistry.getLoadedStubCount(), StubClassRegistry.getFailedStubCount());

            // Apply compatibility fixes for known problematic mods
            LOGGER.info("[{}] Applying compatibility fixes for known problematic mods...", MOD_ID);
            ExceptionHandler.execute(() -> {
                CompatRegistry.applyFixes("create");
                CompatRegistry.applyFixes("kubejs");
                CompatRegistry.applyFixes("apotheosis");
                CompatRegistry.applyFixes("ars_nouveau");
            }, "applying compatibility fixes");

            // Run test to verify stub class system
            TestClientBlocker.runTest();
        }
    }


    static {
        NeoForge.EVENT_BUS.addListener(CommandRegistrationHandler::registerCommands);
    }

    public static class CommandRegistrationHandler {
        public static void registerCommands(RegisterCommandsEvent event) {
            final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
            dispatcher.register(
                    // The existing modlist command registration:
                    Commands.literal("modlist")
                            .requires(source -> true) // Allow all players to use the command.
                            .executes(context -> {
                                CommandSourceStack source = context.getSource();
                                sendModList(source);
                                return 1;
                            })
                            .then(Commands.literal("log")
                                    .executes(context -> {
                                        CommandSourceStack source = context.getSource();
                                        List<String> logEntries = sendModList(source);

                                        try {
                                            Path modsFolder = Paths.get("mods");
                                            if (!Files.exists(modsFolder)) {
                                                Files.createDirectories(modsFolder);
                                            }
                                            Path logFile = modsFolder.resolve("modlist.txt");

                                            Files.write(logFile, logEntries, StandardCharsets.UTF_8,
                                                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                                            Path currentDir = Paths.get(System.getProperty("user.dir"));
                                            String displayPath;
                                            if (currentDir.getParent() != null && currentDir.getParent().getParent() != null) {
                                                Path twoDirsUp = currentDir.getParent().getParent();
                                                Path relativePath = twoDirsUp.relativize(logFile.toAbsolutePath());
                                                displayPath = ".\\" + relativePath.toString();
                                            } else {
                                                displayPath = logFile.toAbsolutePath().toString();
                                            }

                                            source.sendSuccess(() ->
                                                    Component.literal("Mod list logged to " + displayPath), false);
                                        } catch (IOException e) {
                                            source.sendFailure(Component.literal("Error writing mod list: " + e.getMessage()));
                                        }
                                        return 1;
                                    })
                            )
            );

            // Command to check the client feature status.
            dispatcher.register(
                    Commands.literal("clientstatus")
                            .requires(source -> true)
                            .executes(context -> {
                                CommandSourceStack source = context.getSource();
                                String status;
                                if (ClientBlockerSF.CLIENT_FEATURE instanceof ClientFeatureImpl) {
                                    status = "Client features are enabled (client mode).";
                                } else if (ClientBlockerSF.CLIENT_FEATURE instanceof NoOpClientFeature) {
                                    status = "Client features are disabled (server mode).";
                                } else {
                                    status = "Unknown client feature status.";
                                }
                                source.sendSuccess(() -> Component.literal(status), false);
                                return 1;
                            })
            );

            // Command to list all registered stub classes
            dispatcher.register(
                    Commands.literal("liststubs")
                            .requires(source -> true)
                            .executes(context -> {
                                CommandSourceStack source = context.getSource();
                                List<String> stubs = StubClassRegistry.getLoadedStubs();

                                if (stubs.isEmpty()) {
                                    source.sendSuccess(() -> Component.literal("No stub classes have been loaded yet."), false);
                                } else {
                                    source.sendSuccess(() -> Component.literal("Loaded stub classes ("+stubs.size()+"):"), false);
                                    for (String stub : stubs) {
                                        source.sendSuccess(() -> Component.literal(" - " + stub), false);
                                    }
                                }
                                return 1;
                            })
            );
        }

        private static List<String> sendModList(CommandSourceStack source) {
            List<String> logEntries = new ArrayList<>();
            ModList.get().getMods().forEach(modInfo -> {
                String displayName = modInfo.getDisplayName();
                ModList.get().getModContainerById(modInfo.getModId()).ifPresent(modContainer -> {
                    String fileName = modContainer.getModInfo().getOwningFile().getFile().getFileName();
                    String chatEntry = displayName + " | " + fileName;
                    source.sendSuccess(() -> Component.literal(chatEntry), false);
                    String cleanedDisplay = displayName.replaceAll("§.", "");
                    String logEntry = cleanedDisplay + " | " + fileName;
                    logEntries.add(logEntry);
                });
            });
            return logEntries;
        }
    }
}