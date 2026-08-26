# Building

## Requirements

- 64-bit JDK 17
- Internet access on the first build so Gradle/Forge/Minecraft dependencies can be cached

The included `gradlew.bat` and `gradlew` are self-bootstrapping launchers pinned to Gradle 8.1.1. They verify the Gradle distribution SHA-256 before execution. They are not the stock binary Gradle wrapper JAR.

## M2 Windows path

Run:

```text
VERIFY_M2_BUILD.bat
```

A pass produces:

```text
build\libs\matteroverdrive-0.8.0.0-alpha.4.1.jar
```

Launch the development client with:

```text
RUN_M2_CLIENT.bat
```

Run the dedicated-server gate with:

```text
RUN_M2_SERVER.bat
```

For the exact functional test, read `M2_VERIFICATION_PATH.md`.

## Direct Gradle commands

```text
gradlew.bat verifyM1Resources
gradlew.bat verifyM2Sources
gradlew.bat clean build
gradlew.bat runClient
gradlew.bat --console=plain runServer
```

The target is Minecraft 1.20.1, Forge 47.4.10 and Java 17.
