package net.ShockFox05.ClientBlockerSF.stub;

/**
 * Stub implementation of net.minecraft.client.KeyMapping
 * This provides a minimal implementation to prevent crashes on dedicated servers.
 */
public class KeyMappingStub {
    private final String name;
    private final int keyCode;
    private final String category;
    
    public KeyMappingStub(String name, int keyCode, String category) {
        this.name = name;
        this.keyCode = keyCode;
        this.category = category;
    }
    
    public String getName() {
        return name;
    }
    
    public int getKeyCode() {
        return keyCode;
    }
    
    public String getCategory() {
        return category;
    }
    
    public boolean isDown() {
        return false; // Always return false on server
    }
    
    public boolean isPressed() {
        return false; // Always return false on server
    }
}
