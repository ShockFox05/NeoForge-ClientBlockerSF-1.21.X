package net.ShockFox05.ClientBlockerSF;

import com.mojang.brigadier.CommandDispatcher;
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
        // Register config
        modEventBus.register(Config.class);
        modEventBus.addListener(this::onConfigLoad);

        // Register server event listeners
        NeoForge.EVENT_BUS.register(this);

        // Initialize stub class transformer early
        if (FMLEnvironment.dist.isDedicatedServer()) {
            StubClassTransformerHook.init();
        }

        // Invoke client-specific setup (this call is a no-op on a dedicated server).
        CLIENT_FEATURE.performClientSetup();
    }

    private void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(MOD_ID)) {
            LOGGER.info("[ClientBlockerSF] Config loaded");
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            LOGGER.info("[ClientBlockerSF] Running on dedicated server. Scanning for client-only classes...");

            String[] clientOnlyClasses = {
                    "net.minecraft.client.Minecraft",
                    "net.minecraft.client.gui.screens.Screen",
                    "com.mojang.blaze3d.systems.RenderSystem",
                    "net.minecraft.client.KeyMapping",
                    "com.mojang.blaze3d.vertex.BufferBuilder"
            };

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (String className : clientOnlyClasses) {
                String resourceName = className.replace('.', '/') + ".class";
                if (classLoader.getResource(resourceName) != null) {
                    LOGGER.warn("[ClientBlockerSF] WARNING: Client-only class '" + className + "' is present on server. Stub implementation will be provided.");
                } else {
                    LOGGER.info("[ClientBlockerSF] Verified: " + className + " is not present on server (as expected).");
                }
            }

            // Add a command to list all registered stub classes
            LOGGER.info("[ClientBlockerSF] Stub class system is active. Client-side classes will be stubbed to prevent crashes.");
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