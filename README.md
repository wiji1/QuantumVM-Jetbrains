# QuantumVM — OpenQASM Plugin for JetBrains IDEs

OpenQASM 3.0 support for JetBrains IDEs with LSP integration and runtime execution.

## Features

- **Syntax Highlighting** - TextMate-based syntax highlighting for `.qasm` and `.qasm3` files
- **LSP Integration** - Language Server Protocol support via [QuantumVM](https://github.com/wiji1/QuantumVM)
    - Code completion
    - Diagnostics and error checking
    - Hover documentation
    - Go to definition
- **Runtime Execution** - Run QASM files with QuantumVM runtime
- **Auto-Updates** - Automatic binary downloads and updates from GitHub releases
- **Configurable** - Customize LSP settings and binary paths

## Installation

### From JetBrains Marketplace (Recommended)

1. Go to the [JetBrains Marketplace Plugin Page](https://plugins.jetbrains.com/plugin/32494-quantumvm/)
2. Ensure your browser has permission to interact with your IDE directly
3. Click "Install to IDE"
4. Select your IDE and use it to finish the installation

### Manual Installation

1. Download the latest plugin ZIP from [Releases](https://github.com/wiji1/QuantumVM-Jetbrains/releases)
2. Go to Settings → Plugins → ⚙️ → Install Plugin from Disk
3. Select the downloaded ZIP file
4. Restart the IDE

## Requirements

- JetBrains IDE version 2024.3 or later
- Java 21 or later (bundled with the IDE)

## Usage

1. Open or create a `.qasm` file
2. The plugin will automatically:
    - Download the qasm-lsp binary (first run only)
    - Start the language server
    - Provide syntax highlighting and LSP features

### Running QASM Files

1. Right-click a `.qasm` file → Run
2. Or use the gutter icon to run individual files
3. Output appears in the Run tool window

### Settings

Configure the plugin at Settings → Tools → OpenQASM:
- Enable/disable auto-updates
- Custom binary paths for LSP server and QuantumVM runtime
- LSP trace level for debugging

## Development

### Building from Source

```bash
git clone https://github.com/wiji1/QuantumVM-Jetbrains.git
cd QuantumVM-Jetbrains
./gradlew buildPlugin
```

The built plugin will be in `build/distributions/`.

### Running in IDE Sandbox

```bash
./gradlew runIde
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Links

- [QuantumVM Repository](https://github.com/wiji1/QuantumVM)
- [JetBrains Marketplace Plugin Page](https://plugins.jetbrains.com/plugin/32494-quantumvm/)
