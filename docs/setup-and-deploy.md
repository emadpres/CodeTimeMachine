# [Setting Up a Development Environment for Plugin development](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/using_dev_kit.html)
There are two possible workflows for building IntelliJ IDEA plugins:
1. **Gradle**: The recommended workflow for new projects is to use _Gradle_.
2. **Plugin DevKit**: For existing projects, the old workflow using Plugin DevKit is still supported. Plugin DevKit is an IntelliJ plugin that provides support for developing IntelliJ plugins using IntelliJ IDEA’s own build system. Here we explain how to setup your enviroment to use this paradigm.

## I. [Setting Up a Development Environment](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html)
1. Make sure `Plugin DevKit` plugin is installed.
2. In project Structure (`File | Project Structure`) windows, under `Project SDK`:
    1. Select `Add SDK > IntelliJ Platform Plugin SDK ...`
       - If no `JDK` is yet setup for the project, you will be intruppted and you have to first set it up with `Add SDK > JDK...`.
    2. Specify the installation folder of IntelliJ IDEA (**NOT** the source code): `/Applications/IntelliJ IDEA CE.app/Contents` or `C:\Program Files\JetBrains\IntelliJ IDEA 142.3050.1`
    3. Select your desired Java version
3. In the same section, specify the Sandbox Home directory path,  like: `/Users/emadpres/Library/Caches/IdeaIC2017.3/plugins-sandbox`
4. (optional) In `Project Structure | Platform Setting | SDKs` section, select IntelliJ IDEA from the list. Under _Sourcepath_ tab, click the Add button and Specify the **source code** directory for the IntelliJ IDEA Community Edition.
    - You will find the _IntelliJ IDEA CE_ source code [here](https://github.com/JetBrains/intellij-community)


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
