plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        plugin("com.redhat.devtools.lsp4ij", "0.14.2")
        bundledPlugin("org.jetbrains.plugins.textmate")
        pluginVerifier()
        instrumentationTools()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        description = """
            OpenQASM 3.0 support for JetBrains IDEs with LSP integration and runtime execution.

            Features:
            - Syntax highlighting for .qasm files using TextMate grammar
            - Language Server Protocol (LSP) integration with auto-downloaded qasm-lsp binary
            - Run QASM files with QuantumVM runtime
            - Auto-update system for binaries from GitHub
            - Configurable settings for LSP and runtime
        """.trimIndent()

        changeNotes = """
            <h3>1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>OpenQASM 3.0 syntax highlighting</li>
                <li>LSP integration with qasm-lsp</li>
                <li>Runtime execution with QuantumVM</li>
                <li>Auto-update system for binaries</li>
            </ul>
        """.trimIndent()

        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            ide(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    wrapper {
        gradleVersion = "8.10"
        distributionType = Wrapper.DistributionType.ALL
    }
}
