package net.ShockFox05.ClientBlockerSF;

public class NoOpClientFeature implements IClientFeature {
    @Override
    public void performClientSetup() {
        // On a dedicated server, there is no client functionality to perform.
        System.out.println("[ClientBlockerSF] Dedicated server detected. Client features are disabled.");
    }
}
