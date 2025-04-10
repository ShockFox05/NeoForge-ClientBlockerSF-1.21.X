package net.ShockFox05.ClientBlockerSF.stub;

/**
 * Stub implementation of net.minecraft.client.gui.screens.Screen
 * This provides a minimal implementation to prevent crashes on dedicated servers.
 */
public class ScreenStub {
    // Basic properties that might be accessed
    protected int width;
    protected int height;
    protected String title;
    
    // Constructor
    public ScreenStub() {
        this.width = 0;
        this.height = 0;
        this.title = "Stub Screen";
    }
    
    // Common methods that might be called
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public String getTitle() {
        return title;
    }
    
    // Add any other methods that are commonly accessed by mods
}
