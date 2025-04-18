# BackportedCompletions

BackportedCompletions is a Minecraft mod that brings the 1.13+ suggestion box/autocomplete feature to Minecraft 1.8.9. This mod enhances the chat experience by providing a modern, user-friendly suggestion box for commands and player names, similar to newer versions of Minecraft.

## Screenshots

### Suggestion Box in Action
![backportedcompletions_actions](https://github.com/user-attachments/assets/c6eba221-911a-43b3-95f1-5e93025b9717)


### Suggestion Box mouse scrolling
![backportedcompletions_scroll](https://github.com/user-attachments/assets/ca117da3-f517-4467-b2fc-2d062f306b4d)

### Autocomplete Example
![backportedcompletions_box](https://github.com/user-attachments/assets/39851c61-8c7a-43e8-a675-d6272513cb7d)


## Features
- **1.13+ Style Suggestion Box**: Implements the modern autocomplete UI for commands and player names.
- **Customizable Behavior**: Supports case sensitivity and matching options (e.g., match start only).
- **Smooth Integration**: Works seamlessly with Minecraft 1.8.9's chat system.
- **Debounced Autocomplete Requests**: Reduces unnecessary server requests for better performance.
- **Mouse and Keyboard Support**: Navigate suggestions using arrow keys, tab, or mouse clicks.

## How It Works
BackportedCompletions uses [Mixin](https://github.com/SpongePowered/Mixin) to inject functionality into Minecraft's `GuiChat` class. It overrides and extends the behavior of the chat input field to provide the suggestion box.

## Installation
1. Download the mod's `.jar` file from the [Releases](https://github.com/lidanthedev/BackportedCompletions/releases) page.
2. Place the `.jar` file in your Minecraft `mods` folder.
3. Ensure you have a compatible version of [Minecraft Forge](https://files.minecraftforge.net/) installed for 1.8.9.

## Building from Source
To build the mod from source, follow these steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/lidanthedev/BackportedCompletions.git
   cd BackportedCompletions
   ```

2. Compile the mod using Gradle:
   ```bash
   ./gradlew build
   ```

3. The compiled `.jar` file will be located in the `build/libs` directory.

## Development
This mod uses:
- **Java**: The primary programming language.
- **Gradle**: For building and dependency management.
- **Mixin**: For modifying Minecraft's behavior.

### Prerequisites
- Java 8
- Gradle
- IntelliJ IDEA (recommended IDE)

### Setting Up the Development Environment
1. Import the project into IntelliJ IDEA.
2. Run the Gradle `genIntelliJRuns` task to set up the environment.
3. Use the `runClient` Gradle task to test the mod in a development environment.

## Contributing
Contributions are welcome! Feel free to open issues or submit pull requests to improve the mod.

## License
This project is licensed under the [MIT License](LICENSE).

## Acknowledgments
- **Mixin**: For enabling runtime modifications.
- **Forge**: For providing the modding platform.
- **Minecraft Community**: For inspiration and support.

Enjoy the modern chat experience in Minecraft 1.8.9 with BackportedCompletions!
