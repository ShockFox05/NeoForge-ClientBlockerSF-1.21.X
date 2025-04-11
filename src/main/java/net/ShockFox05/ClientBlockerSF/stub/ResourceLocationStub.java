package net.ShockFox05.ClientBlockerSF.stub;

/**
 * Stub for client-side resource location classes.
 */
public class ResourceLocationStub {
    private final String namespace;
    private final String path;
    
    public ResourceLocationStub(String resourceName) {
        String[] parts = resourceName.split(":", 2);
        this.namespace = parts.length > 1 ? parts[0] : "minecraft";
        this.path = parts.length > 1 ? parts[1] : parts[0];
    }
    
    public ResourceLocationStub(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public String getPath() {
        return path;
    }
    
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResourceLocationStub)) return false;
        ResourceLocationStub other = (ResourceLocationStub) obj;
        return namespace.equals(other.namespace) && path.equals(other.path);
    }
    
    @Override
    public int hashCode() {
        return 31 * namespace.hashCode() + path.hashCode();
    }
}
