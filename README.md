# ClientBlockerSF

A Minecraft NeoForge mod that helps with server startup by handling client-side code issues.

## Version 1.1.0

This version includes significant improvements to the stub class system:

- Fixed issues with config loading that could cause server crashes
- Improved the robustness of the stub class system
- Added safety checks to prevent accessing config values before they're loaded
- Enhanced error handling throughout the mod

## Features

- **Client/Server Detection**: Automatically detects whether it's running on a client or a dedicated server.
- **Safe Client Feature Initialization**: Client-specific code is only initialized when running on a client, preventing server crashes.
- **Stub Class System**: Provides stub implementations for client-only classes when running on a dedicated server, preventing crashes when other mods try to access client-only classes.
- **Commands**:
  - `/clientstatus`: Shows whether client features are enabled or disabled.
  - `/modlist`: Lists all installed mods.
  - `/modlist log`: Saves the mod list to a file.
  - `/liststubs`: Lists all client-only classes that have been stubbed.

## Configuration

The mod includes configuration options that can be adjusted in the `clientblockersf-common.toml` file:

```toml
[general]
	# Enable stub class system to prevent crashes when client-only classes are accessed on a server
	enableStubClasses = true
	# Log when stub classes are loaded
	logStubLoading = true
```

## How It Works

When running on a dedicated server, the mod:

1. Detects that it's running on a server and initializes the stub class system.
2. Registers stub implementations for common client-only classes like `Screen`, `KeyMapping`, and `BufferBuilder`.
3. When another mod tries to access a client-only class, the stub class system intercepts the request and provides a minimal implementation instead of crashing.
4. The `/liststubs` command can be used to see which client-only classes have been stubbed.

## Adding More Stubs

If you encounter crashes with other client-only classes, you can add more stub implementations by:

1. Creating a new stub class in the `net.ShockFox05.ClientBlockerSF.stub` package.
2. Registering the stub in the `StubClassTransformer.registerStubClasses()` method.

## Troubleshooting

If you encounter issues with the mod:

1. **Check the server logs**: Look for messages from `[ClientBlockerSF]` to see if there are any warnings or errors.

2. **Use the `/liststubs` command**: This will show you which client-only classes have been stubbed.

3. **Check your configuration**: Make sure the stub class system is enabled in the config file.

4. **Compatibility issues**: If you're experiencing crashes with specific mods, you may need to add more stub implementations for the client-only classes they're trying to access.

## Changelog

### Version 1.1.0
- Fixed issues with config loading that could cause server crashes
- Improved the robustness of the stub class system
- Added safety checks to prevent accessing config values before they're loaded
- Enhanced error handling throughout the mod

### Version 1.0.0
- Initial release

## License

This mod is available under the MIT License.

## Development Resources

Community Documentation: https://docs.neoforged.net/
NeoForged Discord: https://discord.neoforged.net/
