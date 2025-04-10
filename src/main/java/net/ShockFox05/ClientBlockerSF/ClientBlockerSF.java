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
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

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

    public ClientBlockerSF(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        // Register instance-based events.
        NeoForge.EVENT_BUS.register(this);

        // Invoke client-specific setup (this call is a no-op on a dedicated server).
        CLIENT_FEATURE.performClientSetup();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            LOGGER.info("[ClientBlockerSF] Running on dedicated server. Scanning for client-only classes...");

            String[] clientOnlyClasses = {
                    "net.minecraft.client.Minecraft",
                    "net.minecraft.client.gui.screens.Screen",
                    "com.mojang.blaze3d.systems.RenderSystem"
            };

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (String className : clientOnlyClasses) {
                String resourceName = className.replace('.', '/') + ".class";
                if (classLoader.getResource(resourceName) != null) {
                    LOGGER.warn("[ClientBlockerSF] WARNING: Client-only class '" + className + "' is present on server. Disabling client features.");
                    // Optionally, add additional fallback or disabling logic here.
                } else {
                    LOGGER.info("[ClientBlockerSF] Verified: " + className + " is not present on server (as expected).");
                }
            }
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

            // New command to check the client feature status.
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