package net.ShockFox05.ClientBlockerSF.stub;

import net.ShockFox05.ClientBlockerSF.ClientBlockerSF;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps client-side classes to their stub implementations.
 */
public class StubClassMapping {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Class<?>> STUB_CLASSES = new HashMap<>();
    private static final Set<String> CLIENT_PACKAGES = new HashSet<>();

    static {
        // Register stub classes
        register("net.minecraft.client.KeyMapping", KeyMappingStub.class);
        register("net.minecraft.client.gui.screens.Screen", ScreenStub.class);
        register("com.mojang.blaze3d.vertex.BufferBuilder", BufferBuilderStub.class);
        register("net.minecraft.client.model.geom.ModelPart", ModelRendererStub.class);
        register("net.minecraft.client.renderer.RenderType", Object.class);
        register("net.minecraft.client.renderer.GameRenderer", Object.class);
        register("net.minecraft.client.renderer.texture.TextureAtlas", Object.class);
        register("net.minecraft.client.renderer.texture.TextureManager", Object.class);
        register("net.minecraft.client.renderer.entity.EntityRenderer", Object.class);
        register("net.minecraft.client.renderer.entity.EntityRendererProvider", Object.class);
        register("net.minecraft.client.renderer.blockentity.BlockEntityRenderer", Object.class);
        register("net.minecraft.client.renderer.RenderSystem", RenderSystemStub.class);
        register("net.minecraft.client.resources.model.ModelResourceLocation", ResourceLocationStub.class);
        register("net.minecraft.client.Minecraft", Object.class);
        register("net.minecraft.client.gui.Font", Object.class);
        register("net.minecraft.client.gui.GuiGraphics", Object.class);
        register("net.minecraft.client.gui.components.AbstractWidget", Object.class);
        register("net.minecraft.client.gui.components.Button", Object.class);
        register("net.minecraft.client.gui.components.EditBox", Object.class);
        register("net.minecraft.client.gui.components.events.GuiEventListener", Object.class);
        register("net.minecraft.client.gui.narration.NarrationElementOutput", Object.class);
        register("net.minecraft.client.gui.screens.inventory.AbstractContainerScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.inventory.InventoryScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.TitleScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.MenuScreens", Object.class);
        register("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor", Object.class);
        register("net.minecraft.client.gui.screens.worldselection.WorldSelectionList", Object.class);
        register("net.minecraft.client.gui.screens.worldselection.WorldSelectionList$WorldListEntry", Object.class);
        register("net.minecraft.client.gui.screens.worldselection.SelectWorldScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.worldselection.CreateWorldScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.worldselection.EditWorldScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerSelectionList", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$Entry", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$OnlineServerEntry", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$NetworkServerEntry", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$LanServerEntry", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$AddServerEntry", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.EditServerScreen", ScreenStub.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerStatusPinger", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.ServerData", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.LanServerDetection", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.LanServerDetection$LanServerList", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.LanServerDetection$LanServerDetector", Object.class);
        register("net.minecraft.client.gui.screens.multiplayer.LanServerDetection$LanServer", Object.class);

        // Register client packages
        registerClientPackage("net.minecraft.client");
        registerClientPackage("com.mojang.blaze3d");
    }

    public static void register(String className, Class<?> stubClass) {
        LOGGER.debug("[{}] Registering stub class for: {}", ClientBlockerSF.MOD_ID, className);
        STUB_CLASSES.put(className, stubClass);
    }

    public static Class<?> getStubClass(String className) {
        return STUB_CLASSES.get(className);
    }

    public static boolean hasStubClass(String className) {
        return STUB_CLASSES.containsKey(className);
    }

    public static void registerClientPackage(String packageName) {
        LOGGER.debug("[{}] Registering client package: {}", ClientBlockerSF.MOD_ID, packageName);
        CLIENT_PACKAGES.add(packageName);
    }

    public static boolean isClientClass(String className) {
        // Check if the class is in a known client package
        for (String packageName : CLIENT_PACKAGES) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }

        // Check if we have a stub for this class
        return hasStubClass(className);
    }

    /**
     * Get all mapped client class names.
     *
     * @return A set of all mapped client class names
     */
    public static Set<String> getAllMappedClasses() {
        return new HashSet<>(STUB_CLASSES.keySet());
    }
}
