package net.ShockFox05.ClientBlockerSF.stub;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Custom ClassLoader that provides stub implementations for client-only classes.
 */
public class StubClassLoader extends ClassLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public StubClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Check if we have a stub for this class and if the stub system is enabled
        if (StubClassTransformer.hasStubFor(name) && StubClassTransformerHook.isEnabled()) {
            byte[] bytecode = StubClassTransformer.getStubBytecode(name);
            if (bytecode != null) {
                // Log if enabled in config
                if (StubClassTransformerHook.shouldLogLoading()) {
                    LOGGER.info("[ClientBlockerSF] Providing stub implementation for client class: " + name);
                }

                Class<?> stubClass = defineClass(name, bytecode, 0, bytecode.length);

                // Register this stub in the registry for tracking
                StubClassRegistry.registerLoadedStub(name, stubClass.getName());

                return stubClass;
            }
        }

        // If no stub is available, delegate to parent class loader
        return super.findClass(name);
    }
}
