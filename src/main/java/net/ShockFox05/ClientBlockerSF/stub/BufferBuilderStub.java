package net.ShockFox05.ClientBlockerSF.stub;

/**
 * Stub implementation of com.mojang.blaze3d.vertex.BufferBuilder
 * This provides a minimal implementation to prevent crashes on dedicated servers.
 */
public class BufferBuilderStub {
    // Basic properties
    private int vertexCount;
    private boolean building;
    
    public BufferBuilderStub(int initialCapacity) {
        this.vertexCount = 0;
        this.building = false;
    }
    
    // Common methods
    public void begin(int mode, int format) {
        this.building = true;
    }
    
    public void end() {
        this.building = false;
    }
    
    public boolean isBuilding() {
        return building;
    }
    
    public int getVertexCount() {
        return vertexCount;
    }
    
    // Add other methods as needed
}
