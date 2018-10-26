# [Setting Up a Development Environment for Plugin development](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/using_dev_kit.html)
There are two possible workflows for building IntelliJ IDEA plugins. The recommended workflow for new projects is to use *Gradle*. For existing projects, the old workflow using *Plugin DevKit* is still supported.
**Plugin DevKit** is an IntelliJ plugin that provides support for developing IntelliJ plugins using IntelliJ IDEA’s own build system. Here we explain how to setup your enviroment to use this paradigm.

## I. [Setting Up a Development Environment](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html)
- **Get IntelliJ IDEA CE source code** on your local computer. For detailed instructions refer to the Getting IntelliJ IDEA Community Edition Source Code section of [Check Out And Build Community Edition](https://github.com/JetBrains/intellij-community/blob/master/README.md). Note that building IntelliJ IDEA CE from source code step in this link is not required for plugin development.
- **Plugin DevKit plugin** must be [enabled in IntelliJ IDEA](https://www.jetbrains.com/help/idea/managing-plugins.html)
- **Configuring IntelliJ Platform SDK**
-- Create a new IntelliJ Platform SDK under `File | Project Structure`
-- Specify the installation folder of IntelliJ IDEA Community Edition as the home directory. (`C:\Program Files\JetBrains\IntelliJ IDEA 142.3050.1` or `/Applications/IntelliJ IDEA CE.app/Contents`)
-- Select **1.8** as the default Java SDK
-- In the `Sourcepath` tab of the `Platform Setting | SDKs` settings, click the Add button and Specify the **source code** directory for the IntelliJ IDEA Community Edition.
-- In the same section, specify the **Sandbox Home** directory path (like `/Users/emadpres/Library/Caches/IdeaIC2017.3/plugins-sandbox`)

# II. [Creating a Plugin Project](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/creating_plugin_project.html)
### To Create an IntelliJ Platform Plugin Project
- On the main menu, choose `File | New | Project`. The New Project wizard starts.
- Select `IntelliJ Platform Plugin` project type
### Or, to Create an IntelliJ Platform Plugin Module (inside an **existing** non-plugin project)
- Select `File | New | Module` and choose the `IntelliJ Platform Plugin` module type
- Go to `File | Project Structure` and select the newly created IntelliJ Platform SDK (section I) as the default SDK for the plugin module


# [Deploying a Plugin](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/deploying_plugin.html)
- Make your project by invoking `Build | Build Project` or `Build | Build Module <module name>`.
- Prepare your plugin for deployment. In the main menu, select `Build | Prepare Plugin Module <module name> for Deployment`.
- If the plugin module does not depend on any libraries, a `.jar` archive will be created. Otherwise, a `.zip` archive will be created including all the plugin libraries specified in the project settings.