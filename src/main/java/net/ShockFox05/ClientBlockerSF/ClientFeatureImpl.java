package net.ShockFox05.ClientBlockerSF;

// This file should ideally only be compiled and loaded on the client side.
public class ClientFeatureImpl implements IClientFeature {
    @Override
    public void performClientSetup() {
        // Place any client-specific initialization logic here.
        // For example, you can reference client-only classes (like Minecraft.getInstance()).
        System.out.println("[ClientBlockerSF] Initializing client-specific features.");
        // Example: Minecraft mc = Minecraft.getInstance();
    }
}
